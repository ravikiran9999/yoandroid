package com.yo.android.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yo.android.helpers.Helper;

/**
 * Created by rajesh on 28/9/16.
 */
public class SquareImageView extends ImageView {
    private Context context;

    public SquareImageView(Context context) {
        super(context);
        this.context = context;
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int maxWidth = Helper.dp(context, 230);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        float ratio = (float) width / maxWidth;
        height = (int) (width / ratio);
        width = maxWidth;
        setMeasuredDimension(width, height);
    }

}
