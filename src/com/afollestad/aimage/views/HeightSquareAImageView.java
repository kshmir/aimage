package com.afollestad.aimage.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * A version of {@link AImageView} that automatically adjusts its width to match the height of
 * loaded images.
 *
 * @author Aidan Follestad
 */
public class HeightSquareAImageView extends AImageView {

    public HeightSquareAImageView(Context context) {
        super(context);
        super.invalidateOnLoad = true;
    }
    public HeightSquareAImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.invalidateOnLoad = true;
    }
    public HeightSquareAImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.invalidateOnLoad = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getDrawable();
        if(d != null) {
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if(height > 0)
                setMeasuredDimension(height, height);
            else
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
