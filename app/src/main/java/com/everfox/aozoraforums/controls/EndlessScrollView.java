package com.everfox.aozoraforums.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by Daniel on 14/01/2017.
 */

public class EndlessScrollView extends ScrollView {
    public interface EndlessScrollListener {
        void onScrollChanged(EndlessScrollView scrollView,int x, int y, int oldx, int oldy);
    }

    private EndlessScrollListener endlessScrollListener  = null;
    public EndlessScrollView(Context context) {
        super(context);
    }

    public EndlessScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EndlessScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollViewListener(EndlessScrollListener endlessScrollListener) {
        this.endlessScrollListener = endlessScrollListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (endlessScrollListener != null) {
            endlessScrollListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }
}
