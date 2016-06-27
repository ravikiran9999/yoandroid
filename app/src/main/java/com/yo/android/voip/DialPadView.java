package com.yo.android.voip;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yo.android.R;

/**
 * View that displays a twelve-key phone dialpad.
 */
public class DialPadView extends LinearLayout {
    private EditText mDigits;
    private ImageButton mDelete;
    private View mBalanceView;
    private ColorStateList mRippleColor;
    private boolean mCanDigitsBeEdited;
    private final int[] mButtonIds = new int[]{R.id.zero, R.id.one, R.id.two, R.id.three,
            R.id.four, R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine, R.id.star,
            R.id.pound};

    public DialPadView(Context context) {
        this(context, null);
    }

    public DialPadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialPadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Dialpad);
        mRippleColor = a.getColorStateList(R.styleable.Dialpad_dialpad_key_button_touch_tint);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setupKeypad();
        mDigits = (EditText) findViewById(R.id.digits);
        mDelete = (ImageButton) findViewById(R.id.deleteButton);
        mBalanceView = findViewById(R.id.txt_balance);
    }

    private void setupKeypad() {
        final int[] numberIds = new int[]{R.string.dialpad_0_number, R.string.dialpad_1_number,
                R.string.dialpad_2_number, R.string.dialpad_3_number, R.string.dialpad_4_number,
                R.string.dialpad_5_number, R.string.dialpad_6_number, R.string.dialpad_7_number,
                R.string.dialpad_8_number, R.string.dialpad_9_number, R.string.dialpad_star_number,
                R.string.dialpad_pound_number};
        final int[] letterIds = new int[]{R.string.dialpad_0_letters, R.string.dialpad_1_letters,
                R.string.dialpad_2_letters, R.string.dialpad_3_letters, R.string.dialpad_4_letters,
                R.string.dialpad_5_letters, R.string.dialpad_6_letters, R.string.dialpad_7_letters,
                R.string.dialpad_8_letters, R.string.dialpad_9_letters,
                R.string.dialpad_star_letters, R.string.dialpad_pound_letters};
        final Resources resources = getContext().getResources();
        FrameLayout dialpadKey;
        TextView numberView;
        TextView lettersView;
        for (int i = 0; i < mButtonIds.length; i++) {
            dialpadKey = (FrameLayout) findViewById(mButtonIds[i]);
            numberView = (TextView) dialpadKey.findViewById(R.id.dialpad_key_number);
            lettersView = (TextView) dialpadKey.findViewById(R.id.dialpad_key_letters);
            final String numberString = resources.getString(numberIds[i]);
            final RippleDrawable rippleBackground =
                    (RippleDrawable) getContext().getDrawable(R.drawable.btn_dialpad_key);
            if (mRippleColor != null) {
                rippleBackground.setColor(mRippleColor);
            }
            numberView.setText(numberString);
            numberView.setElegantTextHeight(false);
            dialpadKey.setContentDescription(numberString);
            dialpadKey.setBackground(rippleBackground);
            if (lettersView != null) {
                lettersView.setText(resources.getString(letterIds[i]));
            }
        }
    }


    /**
     * Whether or not the digits above the dialer can be edited.
     *
     * @param canBeEdited If true, the backspace button will be shown and the digits EditText
     *                    will be configured to allow text manipulation.
     */
    public void setCanDigitsBeEdited(boolean canBeEdited) {
        View deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setVisibility(canBeEdited ? View.VISIBLE : View.GONE);
        View overflowMenuButton = findViewById(R.id.txt_call_rate);
        overflowMenuButton.setVisibility(canBeEdited ? View.VISIBLE : View.GONE);
        EditText digits = (EditText) findViewById(R.id.digits);
        digits.setClickable(canBeEdited);
        digits.setLongClickable(canBeEdited);
        digits.setFocusableInTouchMode(canBeEdited);
        digits.setCursorVisible(false);
        mCanDigitsBeEdited = canBeEdited;
    }

    public boolean canDigitsBeEdited() {
        return mCanDigitsBeEdited;
    }

    /**
     * Always returns true for onHoverEvent callbacks, to fix problems with accessibility due to
     * the dialpad overlaying other fragments.
     */
    @Override
    public boolean onHoverEvent(MotionEvent event) {
        return true;
    }

    public EditText getDigits() {
        return mDigits;
    }

    public ImageButton getDeleteButton() {
        return mDelete;
    }

    public View getBalanceView() {
        return mBalanceView;
    }


}