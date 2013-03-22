package com.afollestad.aimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.LruCache;
import com.afollestad.aimage.cache.DiskCache;
import com.afollestad.aimage.cache.IOUtils;

import java.io.*;
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
        mDiskCache = new DiskCache(context);
    }


    private int fallbackImageId;
    public final boolean DEBUG = false;
    private Context context;
    private DiskCache mDiskCache;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private LruCache<String, Bitmap> mLruCache = newConfiguredLruCache();
    private ExecutorService mNetworkExecutorService = newConfiguredThreadPool();
    private ExecutorService mDiskExecutorService = Executors.newCachedThreadPool(new LowPriorityThreadFactory());

    protected static final int MEM_CACHE_SIZE_KB = (int) (Runtime.getRuntime().maxMemory() / 2 / 1024);
    protected static final int ASYNC_THREAD_COUNT = (Runtime.getRuntime().availableProcessors() * 4);
    public static final String SOURCE_FALLBACK = "aimage://fallback_image";

    protected void log(String message) {
        if (!DEBUG)
            return;
        Log.i("AImage.ImageManager", message);
    }


    /**
     * Sets the directory that will be used to cache images.
     */
    public ImageManager setCacheDirectory(File cacheDir) {
        if (cacheDir != null)
            mDiskCache.setCacheDirectory(cacheDir);
        return this;
    }

    /**
     * Sets the resource ID of fallback image that is used when an image can't be loaded, or when you call
     * {@link com.afollestad.aimage.views.AImageView#showFallback()} from the AImageView.
     */
    public ImageManager setFallbackImage(int resourceId) {
        this.fallbackImageId = resourceId;
        return this;
    }


    private Bitmap get(String source, Dimension dimension) {
        if (source == null) {
            return null;
        }
        String key = Utils.getKey(source, dimension);
        Bitmap bitmap = mLruCache.get(key);
        if (bitmap == null) {
            bitmap = getBitmapFromDisk(key);
        } else {
            log("Got " + source + " from the memory cache.");
        }
        if (bitmap == null) {
            bitmap = getBitmapFromExternal(key, source, dimension);
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
     * @param callback The callback that the result will be posted to.
     */
    public void get(final String source, final ImageListener callback, final Dimension dimension) {
        get(source, callback, dimension, false);
    }

    private void postCallback(final ImageListener callback, final String source, final Bitmap bitmap) {
        mHandler.post(new Runnable() {
            public void run() {
                if (callback != null)
                    callback.onImageReceived(source, bitmap);
            }
        });
    }

    /**
     * Gets an image from a URI on a separate thread and posts the results to a callback.
     *
     * @param source   The URI to get the image from.
     * @param callback The callback that the result will be posted to.
     */
    public void get(final String source, final ImageListener callback, final Dimension dimension, final boolean isNotifying) {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new RuntimeException("This must only be executed on the main UI Thread!");
        } else if (source == null) {
            return;
        }

        final String key = Utils.getKey(source, dimension);
        Bitmap bitmap = mLruCache.get(key);
        if (bitmap != null) {
            log("Got " + source + " from the memory cache.");
            postCallback(callback, source, bitmap);
            return;
        }

        mDiskExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = getBitmapFromDisk(key);
                if (bitmap != null) {
                    log("Got " + source + " from the disk cache.");
                    postCallback(callback, source, bitmap);
                    return;
                }

                if (!Utils.isOnline(context) && source.startsWith("http")) {
                    log("Device is offline, getting fallback image...");
                    Bitmap fallback = get(ImageManager.SOURCE_FALLBACK, dimension);
                    postCallback(callback, source, fallback);
                    return;
                }

                mNetworkExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bitmap = getBitmapFromExternal(key, source, dimension);
                        log("Got " + source + " from external source.");
                        postCallback(callback, source, bitmap);
                    }
                });
            }
        });
    }


    private Bitmap getBitmapFromDisk(String key) {
        Bitmap bitmap = null;
        try {
            bitmap = mDiskCache.get(key);
            if (bitmap != null) {
                mLruCache.put(key, bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap getBitmapFromExternal(String key, String source, Dimension dimension) {
        byte[] byteArray = sourceToBytes(source);
        if (byteArray != null) {
            Bitmap bitmap = Utils.decodeByteArray(byteArray, dimension);
            if (bitmap != null) {
                if (!source.startsWith("content") && !source.startsWith("file")) {
                    // If the source is already from the local disk, don't cache it locally again.
                    try {
                        mDiskCache.put(key, bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mLruCache.put(key, bitmap);
                return bitmap;
            }
        }
        return null;
    }

    private byte[] inputStreamToBytes(InputStream stream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            IOUtils.copy(stream, byteArrayOutputStream);
        } catch (IOException e) {
            IOUtils.closeQuietly(byteArrayOutputStream);
            return null;
        }
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] sourceToBytes(String source) {
        InputStream inputStream = null;
        byte[] toreturn = null;
        boolean shouldGetFallback = false;

        try {
            if (source.equals(ImageManager.SOURCE_FALLBACK)) {
                if (fallbackImageId > 0)
                    inputStream = context.getResources().openRawResource(fallbackImageId);
                else return null;
            } else if (source.startsWith("content")) { 
				inputStream = context.getContentResolver().openInputStream(Uri.parse(source));
            } else if (source.startsWith("file")) {
                Uri uri = Uri.parse(source);
                inputStream = new FileInputStream(new File(uri.getPath()));
            } else {
                inputStream = new URL(source).openConnection().getInputStream();
            }
            toreturn = inputStreamToBytes(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            shouldGetFallback = true;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        if (shouldGetFallback && !source.equals(ImageManager.SOURCE_FALLBACK) && fallbackImageId > 0) {
            try {
                inputStream = context.getResources().openRawResource(fallbackImageId);
                toreturn = inputStreamToBytes(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        return toreturn;
    }

    private static ExecutorService newConfiguredThreadPool() {
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
}