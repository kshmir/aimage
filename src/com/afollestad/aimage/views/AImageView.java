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
 * and load them into the view with the associated width and height. The view waits until the view is measured so the
 * real measured width and height is used when loading the image.
 */
public class AImageView extends ImageView {

    private String source;
    private ImageManager aimage;

    public AImageView(Context context) {
        super(context);
    }
    public AImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        /**
         * This method allows the view to wait until it has been measured (a view won't be measured until
         * right before it becomes visible, which is usually after your code first starts executing. This
         * insures that correct dimensions will be used for the image loading size to optimize memory.
         */
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i("AImageView", "onSizeChanged -- " + w + ":" + h);
        loadFromSource();
    }

    public void setAImageSource(ImageManager aimage, String source) {
        if(aimage == null || source == null) {
            return;
        }
        this.aimage = aimage;
        this.source = source;
        loadFromSource();
    }

    private void loadFromSource() {
        if(aimage == null || source == null) {
            return;
        } else if(getMeasuredWidth() == 0 && getMeasuredHeight() == 0) {
            Log.i("AImageView", "View not measured yet, waiting...");
            // Wait until the view's width and height are measured
            return;
        }
        aimage.get(this.source, new Dimension(getMeasuredWidth(), getMeasuredHeight()), new ImageListener() {
            @Override
            public void onImageReceived(final String source, final Bitmap bitmap) {
                aimage.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        setImageBitmap(bitmap);
                        Log.i("AImageView", source + " set to view.");
                    }
                });
            }
        });
    }
}
