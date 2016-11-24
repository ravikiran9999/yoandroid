package com.yo.android.ui.fragments;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by rajesh on 22/11/16.
 */
public class NonScrollableListView extends GridView {
    public NonScrollableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Do not use the highest two bits of Integer.MAX_VALUE because they are
        // reserved for the MeasureSpec mode
        int heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightSpec);
        getLayoutParams().height = getMeasuredHeight();
    }

}