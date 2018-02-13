package com.yo.android.chat;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by rajesh on 29/9/16.
 */
public class MaxWidthRelativeLayout extends RelativeLayout {

    private int mMaxWidth = LayoutParams.WRAP_CONTENT;

    public MaxWidthRelativeLayout(Context context) {

        super(context);
    }

    public MaxWidthRelativeLayout(Context context, int mMaxWidth) {

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