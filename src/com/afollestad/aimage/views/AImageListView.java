package com.afollestad.aimage.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class AImageListView extends ListView {

    public AImageListView(Context context) {
        super(context);
        init();
    }
    public AImageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public AImageListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        imageViewMap = new ArrayList<Integer>();
        super.setOnScrollListener(new ScrollManager());
        super.setOnTouchListener(new FingerTracker());
        super.setOnItemSelectedListener(new SelectionTracker());
    }

    private int mScrollState;
    private boolean mFingerUp;
    private ArrayList<Integer> imageViewMap;

    private AbsListView.OnScrollListener scrollListener;
    private OnTouchListener touchListener;
    private AdapterView.OnItemSelectedListener selectListener;


    public AImageListView mapAImageView(Integer id) {
        imageViewMap.add(id);
        return this;
    }

    public void updateItems() {
        if(mScrollState == OnScrollListener.SCROLL_STATE_FLING) {
            return;
        }
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View itemView = getChildAt(i);
            for(Integer viewId : imageViewMap) {
                if(itemView.findViewById(viewId) != null) {
                    ((AImageView)itemView.findViewById(viewId)).load();
                }
            }
        }
    }


    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.scrollListener = l;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        this.touchListener = l;
    }

    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        this.selectListener = l;
    }



    private class ScrollManager implements AbsListView.OnScrollListener {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            boolean stoppedFling = mScrollState == SCROLL_STATE_FLING &&
                    scrollState != SCROLL_STATE_FLING;

//            // Stopped flinging, trigger a round of item updates (after
//            // a small delay, just in case).
//            if (stoppedFling) {
//                final Message msg = mHandler.obtainMessage(MESSAGE_UPDATE_ITEMS,
//                        ItemManager.this);
//
//                mHandler.removeMessages(MESSAGE_UPDATE_ITEMS);
//
//                int delay = (mFingerUp ? 0 : DELAY_SHOW_ITEMS);
//                mHandler.sendMessageDelayed(msg, delay);
//
//                mPendingItemsUpdate = true;
//            } else if (scrollState == SCROLL_STATE_FLING) {
//                mPendingItemsUpdate = false;
//                mHandler.removeMessages(MESSAGE_UPDATE_ITEMS);
//            }

            mScrollState = scrollState;

            if (scrollListener != null) {
                scrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            if (scrollListener != null) {
                scrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    }

    private class FingerTracker implements OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int action = event.getAction();

            mFingerUp = (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_CANCEL);

            // If finger is up and view is not flinging, trigger a new round
            // of item updates.
            if (mFingerUp && mScrollState != OnScrollListener.SCROLL_STATE_FLING) {
                updateItems();
            }

            if (touchListener != null) {
                return touchListener.onTouch(view, event);
            }

            return false;
        }
    }

    private class SelectionTracker implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
                updateItems();
            }

            if (selectListener != null) {
                selectListener.onItemSelected(adapterView, view, position, id);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            if (selectListener != null) {
                selectListener.onNothingSelected(adapterView);
            }
        }
    }
}