package com.afollestad.aimage;

import android.content.Context;

/**
 * Holds width and height values.
 */
public class Dimension {

    /**
     * Initializes the Dimension with equal width in height.
     * @param squarePx The value to set for both the width and the height.
     */
    public Dimension(int squarePx) {
        width = squarePx;
        height = squarePx;
    }

    /**
     * Initializes the Dimension with different width and height.
     */
    public Dimension(int widthPx, int heightPx) {
        width = widthPx;
        height = heightPx;
    }

    /**
     * Initializes the Dimension with equal width and height, converts the specified dp value to pixels.
     * @param context The context that is required for dp conversion.
     * @param squareDp The value in dp to set for both the width and height.
     */
    public Dimension(Context context, float squareDp) {
        int px = dpToPx(context, squareDp);
        width = px;
        height = px;
    }

    /**
     * Initializes the Dimension with different width and height, converts the specified dp values to pixels.
     * @param context The context that is required for dp conversion.
     * @param widthDp The value in dp to set for the width.
     * @param heightDp The value in dp to set for the height.
     */
    public Dimension(Context context, float widthDp, float heightDp) {
        width = dpToPx(context, widthDp);
        height = dpToPx(context, heightDp);
    }

    private int width;
    private int height;


    /**
     * Gets the width of the Dimension in pixels.
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the Dimension in pixels.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns true if both the width and height are zero.
     */
    public boolean isZero() {
        return width == 0 && height == 0;
    }


    protected static int dpToPx(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
