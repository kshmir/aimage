package com.afollestad.aimage.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * A version of {@link AImageView} that automatically adjusts its height to keep aspect ratio with the width (even
 * in a RelativeLayout when FILL_PARENT/WRAP_CONTENT/MATCH_PARENT are used).
 */
public class AspectAImageView extends AImageView {

    public AspectAImageView(Context context) {
        super(context);
    }
    public AspectAImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * This method insures the image view keeps it's aspect ratio when the view is stretched
         * (like in a RelativeLayout where fill_parent or wrap_content are used).
         */
        Drawable d = getDrawable();
        if(d != null) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
