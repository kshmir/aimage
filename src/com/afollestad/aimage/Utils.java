package com.afollestad.aimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.afollestad.aimage.cache.DigestUtils;

public class Utils {

    public static int calculateInSampleSize(BitmapFactory.Options options, Dimension dimension) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > dimension.getHeight() || width > dimension.getWidth()) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) dimension.getHeight());
            final int widthRatio = Math.round((float) width / (float) dimension.getWidth());

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static BitmapFactory.Options getBitmapFactoryOptions(Dimension dimension) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if(dimension != null)
            options.inSampleSize = Utils.calculateInSampleSize(options, dimension);
        return options;
    }

    public static Bitmap decodeByteArray(byte[] byteArray, Dimension dimension) {
        try {
            BitmapFactory.Options bitmapFactoryOptions = Utils.getBitmapFactoryOptions(dimension);
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bitmapFactoryOptions);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static boolean isOnline(Context context) {
        if (context == null) {
            return false;
        }
        boolean state = false;
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null) {
            state = wifiNetwork.isConnectedOrConnecting();
        }

        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null) {
            state = mobileNetwork.isConnectedOrConnecting();
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            state = activeNetwork.isConnectedOrConnecting();
        }
        return state;
    }

    public static String getKey(String source, Dimension dimension) {
        if (source == null) {
            return null;
        }
        if (dimension != null)
            source += "_" + dimension.toString();
        return DigestUtils.sha256Hex(source);
    }
}
