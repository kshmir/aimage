package com.afollestad.aimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
}
