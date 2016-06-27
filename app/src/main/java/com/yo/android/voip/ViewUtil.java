package com.yo.android.voip;

import android.graphics.Outline;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.TextView;


/**
 * Provides static functions to work with views
 */
public class ViewUtil {
    private ViewUtil() {
    }

    /**
     * Returns the width as specified in the LayoutParams
     *
     * @throws IllegalStateException Thrown if the view's width is unknown before a layout pass
     *                               s
     */
    public static int getConstantPreLayoutWidth(View view) {
        // We haven't been layed out yet, so get the size from the LayoutParams
        final ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p.width < 0) {
            throw new IllegalStateException("Expecting view's width to be a constant rather " +
                    "than a result of the layout pass");
        }
        return p.width;
    }

    /**
     * Returns a boolean indicating whether or not the view's layout direction is RTL
     *
     * @param view - A valid view
     * @return True if the view's layout direction is RTL
     */
    public static boolean isViewLayoutRtl(View view) {
        return view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private static final ViewOutlineProvider OVAL_OUTLINE_PROVIDER = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setOval(0, 0, view.getWidth(), view.getHeight());
        }
    };

    public static void resizeText(TextView textView, int originalTextSize, int minTextSize) {
        final Paint paint = textView.getPaint();
        final int width = textView.getWidth();
        if (width == 0) return;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
        float ratio = width / paint.measureText(textView.getText().toString());
        if (ratio <= 1.0f) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    Math.max(minTextSize, originalTextSize * ratio));
        }
    }
}