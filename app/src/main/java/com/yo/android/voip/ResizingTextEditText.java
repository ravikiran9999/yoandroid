package com.yo.android.voip;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import com.yo.android.R;

/**
 * EditText which resizes dynamically with respect to text length.
 */
public class ResizingTextEditText extends EditText {
    private final int mOriginalTextSize;
    private final int mMinTextSize;

    public ResizingTextEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mOriginalTextSize = (int) getTextSize();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ResizingText);
        mMinTextSize = (int) a.getDimension(R.styleable.ResizingText_resizing_text_min_size,
                mOriginalTextSize);
        a.recycle();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        ViewUtil.resizeText(this, mOriginalTextSize, mMinTextSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ViewUtil.resizeText(this, mOriginalTextSize, mMinTextSize);
    }
}