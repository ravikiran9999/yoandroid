package com.yo.android.chat;

import android.content.Context;
import android.widget.LinearLayout;

/**
 * Created by rajesh on 27/9/16.
 */
public class MaxWidthLinearLayout extends LinearLayout {

    private int mMaxWidth = LayoutParams.WRAP_CONTENT;

    public MaxWidthLinearLayout(Context context) {

        super(context);
    }

    public MaxWidthLinearLayout(Context context, int mMaxWidth) {

        super(context);

        this.mMaxWidth = mMaxWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //get measured height
        if (getMeasuredWidth() > mMaxWidth) {
            setMeasuredDimension(mMaxWidth, getMeasuredHeight());
        }
    }
}