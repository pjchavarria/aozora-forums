package com.everfox.aozoraforums.controls;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;

/**
 * Created by Daniel on 21/03/2017.
 */

public class AoGridLayoutManager extends GridLayoutManager {

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    public AoGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AoGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public AoGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }
}
