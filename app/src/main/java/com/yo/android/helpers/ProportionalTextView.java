package com.yo.android.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class ProportionalTextView extends TextView {

    public ProportionalTextView(Context context) {
        super(context);
    }

    public ProportionalTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ProportionalTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ProportionalTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        /*Log.i("PTV", "height: " + h);
        int lineHeight = this.getLineHeight()/2;
        Log.i("PTV", "lineHeight: " + lineHeight);*/
        // set fitting lines to prevent cut text
        int fittingLines = h / getLineHeight();
        //Log.i("PTV", "fittingLines: " + fittingLines);
        /*if(h !=0 && h < 100) {
            this.setLines(5);
        } else */

        if (fittingLines > 0) {
            this.setLines(fittingLines);
            this.setEllipsize(TextUtils.TruncateAt.END);
        }

    }

}
