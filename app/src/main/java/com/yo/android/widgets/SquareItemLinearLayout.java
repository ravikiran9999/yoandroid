package com.yo.android.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Ramesh on 21/4/16.
 */
public class SquareItemLinearLayout extends FrameLayout {
    public SquareItemLinearLayout(Context context) {
        super(context);
    }

    public SquareItemLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareItemLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
