package com.afollestad.aimage.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * A version of {@link com.afollestad.aimage.views.AImageView} that automatically adjusts its width to match the height of
 * loaded images.
 *
 * @author Aidan Follestad
 */
public class SquareAImageView extends AImageView {

    public SquareAImageView(Context context) {
        super(context);
        super.invalidateOnLoad = true;
    }
    public SquareAImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.invalidateOnLoad = true;
    }
    public SquareAImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.invalidateOnLoad = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getDrawable();
        if(d != null) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            setMeasuredDimension(width, width);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
