package com.afollestad.aimage.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.afollestad.aimage.Dimension;
import com.afollestad.aimage.ImageListener;
import com.afollestad.aimage.ImageManager;

/**
 * An {@link ImageView} that wraps around the {@link ImageManager} class to asynchronously load images on a separate thread
 * and load them into the view with the associated width and height. The view waits until it's measured so the
 * real measured width and height are used when loading the image to size.
 */
public class AImageView extends ImageView {

    private String source;
    private ImageManager aimage;
    protected boolean invalidateOnLoad;

    public AImageView(Context context) {
        super(context);
    }

    public AImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        /**
         * This method allows the view to wait until it has been measured (a view won't be measured until
         * right before it becomes visible, which is usually after your code first starts executing. This
         * insures that correct dimensions will be used for the image loading size to optimize memory.
         */
        super.onSizeChanged(w, h, oldw, oldh);
        if(aimage != null && aimage.isDebugEnabled())
            Log.i("AImageView", "onSizeChanged -- " + w + ":" + h);
        loadFromSource();
    }


    public AImageView setManager(ImageManager manager) {
        if(manager == null) {
            throw new IllegalArgumentException("The ImageManager cannot be null.");
        }
        this.aimage = manager;
        return this;
    }

    public AImageView setSource(String source) {
        this.source = source;
        return this;
    }

    public void load() {
        loadFromSource();
    }


    protected String lastSource;

    private void loadFromSource() {
        if (aimage == null || source == null) {
            setImageBitmap(null);
            return;
        } else if (getMeasuredWidth() == 0 && getMeasuredHeight() == 0) {
            if(aimage.isDebugEnabled())
                Log.i("AImageView", "View not measured yet, waiting...");
            // Wait until the view's width and height are measured
            return;
        }
        lastSource = source;
        aimage.get(this.source, new Dimension(getMeasuredWidth(), getMeasuredHeight()), new ImageListener() {
            @Override
            public void onImageReceived(final String source, final Bitmap bitmap) {
                if(lastSource != null && !lastSource.equals(source)) {
                    if(aimage.isDebugEnabled())
                        Log.i("AImageView", "View source changed since download started, not setting " + source + " to view.");
                    return;
                }
                setImageBitmap(bitmap);
                if (invalidateOnLoad) {
                    requestLayout();
                    invalidate();
                }
                if(aimage.isDebugEnabled())
                    Log.i("AImageView", source + " set to view.");
            }
        });
    }
}
