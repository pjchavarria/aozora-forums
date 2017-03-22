package com.everfox.aozoraforums.controls;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

/**
 * Created by Daniel on 21/03/2017.
 */

public class AoLinearLayoutManager extends LinearLayoutManager {

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    public AoLinearLayoutManager(Context context) {
        super(context);
    }

    public AoLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public AoLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
