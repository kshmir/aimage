package com.afollestad.aimage.views;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.afollestad.aimage.ImageManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SplitAImageView extends View {

	public SplitAImageView(Context context) {
		super(context);
		initialize();
	}
	public SplitAImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}
	public SplitAImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	private void initialize() {
		this.sources = new ArrayList<String>();
	}
	
	private List<String> sources;
    private ImageManager aimage;
    
    private WeakReference<Bitmap> imageOne;
    private WeakReference<Bitmap> imageTwo;
    private WeakReference<Bitmap> imageThree;
    private WeakReference<Bitmap> imageFour;
    
    static Paint red = new Paint();
	static Paint yellow = new Paint();
	static Paint blue = new Paint();
	static Paint green = new Paint();
	
	static {
		red.setColor(Color.RED);
    	yellow.setColor(Color.YELLOW);
    	blue.setColor(Color.BLUE);
    	green.setColor(Color.GREEN);
	}
	
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        /**
         * This method allows the view to wait until it has been measured (a view won't be measured until
         * right before it becomes visible, which is usually after your code first starts executing. This
         * insures that correct dimensions will be used for the image loading size to optimize memory.
         */
        super.onSizeChanged(w, h, oldw, oldh);
        loadFromSources();
    }
    
	/**
     * Sets the ImageManager that is used to load images into the view.
     */
    public SplitAImageView setManager(ImageManager manager) {
        if(manager == null) {
            throw new IllegalArgumentException("The ImageManager cannot be null.");
        }
        this.aimage = manager;
        return this;
    }

    /**
     * Adds a source to load into the split view.
     */
    public SplitAImageView addSource(String source) {
    	if(sources.size() >= 4)
    		throw new IllegalStateException("You cannot add more than 4 images to the SplitAImageView");
        this.sources.add(source);
        return this;
    }
    
    /**
     * Clears all sources from the split view.
     * @return
     */
    public SplitAImageView clearSources() {
    	this.sources.clear();
    	return this;
    }

    /**
     * Loads an image into the view, using the ImageManager set via #setManager and the source set via #setSource.
     */
    public void load() {
        if(aimage == null)
            throw new IllegalStateException("You cannot call load() on the AImageView until you have set a ImageManager via setManager().");
        loadFromSources();
    }

    @Override
    public void onDraw(Canvas canvas) {
    	int halfAcross = getWidth() / 2;
    	int halfDown = getHeight() / 2;
    	
    	// left top right bottom
    	canvas.drawRect(0, 0, getWidth(), halfDown, red);  // Top
    	//canvas.drawRect(halfAcross, 0, getWidth(), halfDown, yellow);  // Top right
    	canvas.drawRect(0, halfDown, halfAcross, getHeight(), blue); // Bottom left
    	canvas.drawRect(halfAcross, halfDown, getWidth(), getHeight(), green);  // Bottom right
    	
    }
    
    private void loadFromSources() {
        if (aimage == null) {
            return;
        } else if (getMeasuredWidth() == 0 && getMeasuredHeight() == 0) {
            if(aimage.DEBUG)
                Log.i("AImageView", "View not measured yet, waiting...");
            // Wait until the view's width and height are measured
            return;
        }

//        final Dimension dimen = this.fitView ? new Dimension(this) : null;
//        aimage.get(this.source, new ImageListener() {
//            @Override
//            public void onImageReceived(final String source, final Bitmap bitmap) {
//                if(lastSource != null && !lastSource.equals(source)) {
//                    if(aimage.DEBUG)
//                        Log.i("AImageView", "View source changed since download started, not setting " + source + " to view.");
//                    return;
//                }
//
//                // Post on the view's UI thread to be 100% sure we're on the right thread
//                SplitAImageView.this.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        setImageBitmap(bitmap);
//                        if (invalidateOnLoad) {
//                            requestLayout();
//                            invalidate();
//                        }
//                        if(aimage.DEBUG)
//                            Log.i("AImageView", source + " set to view " + Utils.getKey(source, dimen));
//                    }
//                });
//            }
//        }, dimen);
    }
}
