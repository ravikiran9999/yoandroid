package com.greyhound.mobile.consumer.widgets;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.greyhound.mobile.consumer.R;


public class BottomBar extends LinearLayout {

    private OnBottomBarItemClickListener mListener;

    public interface OnBottomBarItemClickListener {
        void onBottomBarItemClick(String tag);
    }

    public interface BottomBarNavigationListener {
        void onBottomBarScreenChanged(String tag);
        void updateNotificationCount();
    }

    public BottomBar(Context context) {
        super(context);
    }

    public BottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListenerForChildren();
    }

    private void setOnClickListenerForChildren() {
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onBottomBarItemClick((String) view.getTag());
                }
            }
        };

        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            TextView child = findBottomBarTabTextView(getChildAt(i));
            child.setOnClickListener(onClickListener);
        }
    }

    public void updateSelectedState(String tag) {
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            TextView child = findBottomBarTabTextView(getChildAt(i));
            flipTabColorState(child, tag.equals(child.getTag()));
        }
    }

    private TextView findBottomBarTabTextView(View view) {
        if (view instanceof FrameLayout) { // Make sure the first child is the TextView tab
            return (TextView) ((FrameLayout) view).getChildAt(0);
        }
        return (TextView) view;
    }

    private void flipTabColorState(TextView tabTextView, boolean isSelected) {
        tabTextView.setTextColor(ContextCompat.getColor(getContext(), isSelected?
                R.color.gh_blue : R.color.font_gh_gray));
        tabTextView.setEnabled(!isSelected);

        Drawable compoundDrawable = tabTextView.getCompoundDrawables()[1];
        if (isSelected) {
            compoundDrawable.setColorFilter(
                    ContextCompat.getColor(getContext(), R.color.gh_blue), PorterDuff.Mode.SRC_IN);
        }
        else {
            compoundDrawable.setColorFilter(null);
        }
        tabTextView.setCompoundDrawables(null, compoundDrawable.mutate(), null, null);
    }

    public void initTypeface(Typeface typeface) {
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            TextView child = findBottomBarTabTextView(getChildAt(i));
            child.setTypeface(typeface);
        }
    }

    public void updateNotificationCount(String tag, int count) {
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View child = getChildAt(i);
            TextView tabTextView = findBottomBarTabTextView(child);
            if (tag.equals(tabTextView.getTag())) {
                TextView countText = (TextView) ((FrameLayout) child).getChildAt(1);
                if (count > 0) {
                    countText.setText(count > 99? "99+" : String.valueOf(count));
                    countText.setVisibility(VISIBLE);
                }
                else {
                    countText.setVisibility(GONE);
                }
            }
        }
    }

    public void setOnBottomBarItemClickListener(OnBottomBarItemClickListener listener) {
        mListener = listener;
    }

}