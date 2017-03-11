package com.everfox.aozoraforums.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by daniel.soto on 1/16/2017.
 */

public class RecyclerItemLongClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemLongClickListener mListener;

    public interface OnItemLongClickListener {
        public void onItemLongClick(View view, int position);
    }

    GestureDetector mGestureDetector;

    public RecyclerItemLongClickListener(Context context, OnItemLongClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent e) {
                return;
            }

        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemLongClick(childView, view.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

}
