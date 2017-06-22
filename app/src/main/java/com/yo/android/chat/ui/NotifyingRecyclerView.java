package com.yo.android.chat.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by rdoddapaneni on 6/20/2017.
 */

public class NotifyingRecyclerView extends RecyclerView {

    private OnScrollChangedListener mOnScrollChangedListener;

    public interface OnScrollChangedListener {
        void onScrollChanged(RecyclerView view, int l, int t);
    }

    public NotifyingRecyclerView(Context context) {
        super(context);
    }

    public NotifyingRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotifyingRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        Log.d("SCROLLL", "onScrolled " + dx + " " + dy);
        if (mOnScrollChangedListener != null && dy != 0) {
            mOnScrollChangedListener.onScrollChanged(this, dx, dy);
        }
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        this.mOnScrollChangedListener = listener;
    }
}
