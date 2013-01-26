package com.afollestad.aimage;

import android.content.Context;

public class Dimension {

    public Dimension(int squarePx) {
        width = squarePx;
        height = squarePx;
    }
    public Dimension(int widthPx, int heightPx) {
        width = widthPx;
        height = heightPx;
    }
    public Dimension(Context context, float squareDp) {
        int px = dpToPx(context, squareDp);
        width = px;
        height = px;
    }
    public Dimension(Context context, float widthDp, float heightDp) {
        width = dpToPx(context, widthDp);
        height = dpToPx(context, heightDp);
    }

    private int width;
    private int height;


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isZero() {
        return width == 0 && height == 0;
    }


    public static int dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
