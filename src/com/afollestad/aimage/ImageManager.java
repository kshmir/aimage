package com.afollestad.aimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import com.afollestad.aimage.cache.DigestUtils;
import com.afollestad.aimage.cache.DiskLruCache;
import com.afollestad.aimage.cache.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.*;

/**
 * <p>The most important class in the AImage library; downloads images, and handles caching them on the disk and in memory
 * so they can quickly be retrieved. Also allows you to download images to fit a certain width and height.</p>
 * <p/>
 * <p>If you're using AImage for displaying images in your UI, see {@link com.afollestad.aimage.views.AImageView} and
 * {@link com.afollestad.aimage.views.AspectAImageView} for easy-to-use options.</p>
 */
public class ImageManager {

    public ImageManager(Context context) {
        this.context = context;
        mLruCache = new LruCache<String, Bitmap>(MEM_CACHE_SIZE_KB * 1024) {
            @Override
            public int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        mDiskLruCache = openDiskCache();
    }

    private boolean debug;
    private Context context;
    private static final Object[] LOCK = new Object[0];
    private DiskLruCache mDiskLruCache;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private LruCache<String, Bitmap> mLruCache = newConfiguredLruCache();
    private ExecutorService mNetworkExecutorService = newConfiguredThreadPool();
    private ExecutorService mDiskExecutorService = Executors.newCachedThreadPool(new LowPriorityThreadFactory());

    protected static final int MEM_CACHE_SIZE_KB = (int) (Runtime.getRuntime().maxMemory() / 2 / 1024);
    protected static final int DISK_CACHE_SIZE_KB = (10 * 1024);
    protected static final int ASYNC_THREAD_COUNT = (Runtime.getRuntime().availableProcessors() * 4);


    public void setDebugEnabled(boolean enabled) {
        debug = enabled;
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    protected void log(String message) {
        if(!debug)
            return;
        Log.i("AImage.ImageManager", message);
    }


    private static String getKey(String source, Dimension dimen) {
        if (source == null) {
            return null;
        }
        if (dimen != null && dimen.getWidth() > 0 && dimen.getHeight() > 0) {
            source += dimen.toString();
        }
        return DigestUtils.sha256Hex(source);
    }

    /**
     * Gets an image from a URI on the calling thread and returns the result.
     *
     * @param source The URI to get the image from.
     * @param dimen  The optional target dimensions that the image will be resized to.
     */
    public Bitmap get(String source, Dimension dimen) {
        if (source == null) {
            return null;
        }
        String key = getKey(source, dimen);
        Bitmap bitmap = mLruCache.get(key);
        if (bitmap == null) {
            bitmap = getBitmapFromDisk(key);
        } else {
            log("Got " + source + " from the memory cache.");
        }
        if (bitmap == null) {
            bitmap = getBitmapFromExternal(key, source, dimen);
            log("Got " + source + " from the external source.");
        } else {
            log("Got " + source + " from the disk cache.");
        }
        return bitmap;
    }

    /**
     * Gets an image from a URI on a separate thread and posts the results to a callback.
     *
     * @param source   The URI to get the image from.
     * @param dimen    The optional target dimensions that the image will be resized to.
     * @param callback The callback that the result will be posted to.
     */
    public void get(final String source, final Dimension dimen, final ImageListener callback) {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new RuntimeException("This must only be executed on the main UI Thread!");
        } else if (source == null) {
            return;
        }
        final String key = getKey(source, dimen);
        Bitmap bitmap = mLruCache.get(key);
        if (bitmap != null && callback != null) {
            log("Got " + source + " from the memory cache.");
            callback.onImageReceived(source, bitmap);
        } else {
            mDiskExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = getBitmapFromDisk(key);
                    if (bitmap != null) {
                        log("Got " + source + " from the disk cache.");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null)
                                    callback.onImageReceived(source, bitmap);
                            }
                        });
                    } else {
                        mNetworkExecutorService.execute(new Runnable() {

                            @Override
                            public void run() {
                                final Bitmap bitmap = getBitmapFromExternal(key, source, dimen);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        log("Got " + source + " from external source.");
                                        if (callback != null)
                                            callback.onImageReceived(source, bitmap);
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
    }

    private Bitmap getBitmapFromDisk(String key) {
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskLruCache.get(key);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                bitmap = decodeFromSnapshot(snapshot);
                if (bitmap != null) {
                    mLruCache.put(key, bitmap);
                }
            }
        }
        return bitmap;
    }

    private Bitmap getBitmapFromExternal(String key, String source, Dimension dimen) {
        byte[] byteArray = copyURLToByteArray(source);
        if (byteArray != null) {
            Bitmap bitmap = decodeByteArray(byteArray, dimen);
            if (bitmap != null) {
                copyBitmapToDiskLruCache(key, bitmap);
                mLruCache.put(key, bitmap);
                return bitmap;
            }
        }
        return null;
    }


    private byte[] copyURLToByteArray(String source) {
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            if (source.startsWith("content")) {
                inputStream = context.getContentResolver().openInputStream(Uri.parse(source));
            } else {
                inputStream = new URL(source).openConnection().getInputStream();
            }
            byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }
        return null;
    }

    private static Bitmap decodeByteArray(byte[] byteArray, Dimension dimen) {
        try {
            Bitmap bitmap;
            BitmapFactory.Options bitmapFactoryOptions = getBitmapFactoryOptions();

            synchronized (LOCK) {
                if (dimen != null && !dimen.isZero()) {
                    bitmapFactoryOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bitmapFactoryOptions);

                    int heightRatio = (int) Math.ceil(bitmapFactoryOptions.outHeight / (float) dimen.getHeight());
                    int widthRatio = (int) Math.ceil(bitmapFactoryOptions.outWidth / (float) dimen.getWidth());

                    if (heightRatio > 1 || widthRatio > 1) {
                        if (heightRatio > widthRatio) {
                            bitmapFactoryOptions.inSampleSize = heightRatio;
                        } else {
                            bitmapFactoryOptions.inSampleSize = widthRatio;
                        }
                    }
                    bitmapFactoryOptions.inJustDecodeBounds = false;
                }

                bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bitmapFactoryOptions);
            }

            return bitmap;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private void copyBitmapToDiskLruCache(String key, Bitmap bitmap) {
        DiskLruCache.Editor editor = null;
        OutputStream outputStream = null;
        try {
            synchronized (LOCK) {
                if (!context.getExternalCacheDir().exists()) {
                    /*
                     * We are in an unexpected state, our cache directory was
                     * destroyed without our static instance being destroyed
                     * also. The best thing we can do here is start over.
                     */
                    mDiskLruCache = openDiskCache();
                }
            }

            /*
             * We block here because Editor.edit will return null if another
             * edit is in progress
             */
            while (editor == null) {
                editor = mDiskLruCache.edit(key);
                Thread.sleep(50);
            }

            outputStream = editor.newOutputStream(0);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStream);
            if (editor != null) {
                try {
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static BitmapFactory.Options getBitmapFactoryOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return options;
    }

    private static Bitmap decodeFromSnapshot(DiskLruCache.Snapshot snapshot) {
        InputStream inputStream = null;
        try {
            inputStream = snapshot.getInputStream(0);
            synchronized (LOCK) {
                return BitmapFactory.decodeStream(inputStream, null, getBitmapFactoryOptions());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(snapshot);
        }
        return null;
    }


    public static ExecutorService newConfiguredThreadPool() {
        int corePoolSize = 0;
        int maximumPoolSize = ASYNC_THREAD_COUNT;
        long keepAliveTime = 60L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    private static LruCache<String, Bitmap> newConfiguredLruCache() {
        return new LruCache<String, Bitmap>(MEM_CACHE_SIZE_KB * 1024) {
            @Override
            public int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    private DiskLruCache openDiskCache() {
        try {
            return DiskLruCache.open(context.getExternalCacheDir(), 1, 1, DISK_CACHE_SIZE_KB * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}