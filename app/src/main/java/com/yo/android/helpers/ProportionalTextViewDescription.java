package com.yo.android.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class ProportionalTextViewDescription extends TextView {

    public ProportionalTextViewDescription(Context context) {
        super(context);
    }

    public ProportionalTextViewDescription(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ProportionalTextViewDescription(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ProportionalTextViewDescription(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int fittingLines = h / getLineHeight();

        if(h != 0 && h <= 150 && fittingLines <= 2) {
            this.setLines(4);
            this.setEllipsize(TextUtils.TruncateAt.END);
        } else if (fittingLines > 0) {
            this.setLines(fittingLines);
            this.setEllipsize(TextUtils.TruncateAt.END);
        }

    }

}
