package com.afollestad.aimage.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.afollestad.aimage.Dimension;
import com.afollestad.aimage.ImageListener;
import com.afollestad.aimage.ImageManager;
import com.afollestad.aimage.Utils;

public class AImageView extends ImageView {

    private String source;
    private ImageManager aimage;
    protected boolean invalidateOnLoad;
    private boolean fitView = true;
    protected String lastSource;
    private View loadingView;

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
        loadFromSource();
    }


    /**
     * Sets the ImageManager that is used to load images into the view.
     */
    public AImageView setManager(ImageManager manager) {
        if(manager == null) {
            throw new IllegalArgumentException("The ImageManager cannot be null.");
        }
        this.aimage = manager;
        return this;
    }

    /**
     * Gets the ImageManager that's used by this view to load and cache images.
     * @return
     */
    public ImageManager getImageManager() {
    	return this.aimage;
    }
    
    /**
     * Sets the source of the image to load into the view.
     */
    public AImageView setSource(String source) {
        this.source = source;
        return this;
    }
    
    /**
     * Gets the source of the last loaded image.
     * @return
     */
    public String getSource() {
    	return this.source;
    }

    /**
     * Turned on by default as it prevents OutOfMemoryExceptions. Sets whether or not the loaded image will be
     * resized to fit the dimensions of the view.
     */
    public AImageView setFitView(boolean fitView) {
        this.fitView = fitView;
        return this;
    }

    /**
     * Sets the view that will become visible when the view begins loading an image, and will be hidden when the 
     * view finishes loading an image. The imageview itself will also be hidden during loading if a loading view is set.
     */
    public AImageView setLoadingView(View view) {
    	this.loadingView = view;
    	return this;
    }
    
    /**
     * Loads an image into the view, using the ImageManager set via #setManager and the source set via #setSource.
     */
    public void load() {
        if(aimage == null)
            throw new IllegalStateException("You cannot call load() on the AImageView until you have set a ImageManager via setManager().");
        loadFromSource();
    }

    /**
     * Loads the fallback image set from the {@link ImageManager} set via #setManager.
     */
    public void showFallback() {
        if(aimage == null)
            throw new IllegalStateException("You cannot load the fallback image until you have set a ImageManager via setManager().");
        aimage.get(ImageManager.SOURCE_FALLBACK, new ImageListener() {
            @Override
            public void onImageReceived(final String source, final Bitmap bitmap) {
                setImageBitmap(bitmap);
                if (invalidateOnLoad) {
                    requestLayout();
                    invalidate();
                }
                if(aimage.isDebugEnabled())
                    Log.i("AImageView", "Fallback image set to view.");
            }
        }, new Dimension(this));
    }


    private void loadFromSource() {
        if (aimage == null) {
            return;
        } else if(source == null || source.trim().isEmpty()) {
            showFallback();
            return;
        } else if (getMeasuredWidth() == 0 && getMeasuredHeight() == 0) {
            if(aimage.isDebugEnabled())
                Log.i("AImageView", "View not measured yet, waiting...");
            // Wait until the view's width and height are measured
            return;
        }

        lastSource = source;
        final Dimension dimen = this.fitView ? new Dimension(this) : null;
        if(loadingView != null) {
        	loadingView.setVisibility(View.VISIBLE);
        	this.setVisibility(View.GONE);
        }
        aimage.get(this.source, new ImageListener() {
            @Override
            public void onImageReceived(final String source, final Bitmap bitmap) {
                if(lastSource != null && !lastSource.equals(source)) {
                    if(aimage.isDebugEnabled())
                        Log.i("AImageView", "View source changed since download started, not setting " + source + " to view.");
                    return;
                }

                // Post on the view's UI thread to be 100% sure we're on the right thread
                AImageView.this.post(new Runnable() {
                    @Override
                    public void run() {
                        setImageBitmap(bitmap);
                        if (invalidateOnLoad) {
                            requestLayout();
                            invalidate();
                        }
                        if(loadingView != null) {
                        	loadingView.setVisibility(View.GONE);
                        	AImageView.this.setVisibility(View.VISIBLE);
                        }
                        if(aimage.isDebugEnabled())
                            Log.i("AImageView", source + " set to view " + Utils.getKey(source, dimen));
                    }
                });
            }
        }, dimen);
    }
}