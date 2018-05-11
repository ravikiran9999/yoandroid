package com.yo.android.helpers;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.model.MoreData;


import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by rdoddapaneni on 6/21/2017.
 */

public class BalanceViewHolder extends RecyclerView.ViewHolder {
    private static final String YOUR_YO_BALANCE = "Your YO! balance";

    @BindView(R.id.title)
    TextView textView;
    @BindView(R.id.image)
    ImageView arrowView;
    @BindView(R.id.text_view)
    TextView balanceView;


    public BalanceViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Object data) {

        MoreData item = (MoreData) data;
        if(item.getName().contains(YOUR_YO_BALANCE)) {
            textView.setText(spannableString(item.getName()));
        } else {
            textView.setText(item.getName());
        }

        if (item.isHasOptions()) {
            arrowView.setVisibility(View.VISIBLE);
        } else {
            arrowView.setVisibility(View.GONE);
        }
        if (item.getBalance() != null) {
            balanceView.setText(item.getBalance());
            balanceView.setVisibility(View.VISIBLE);
        } else {
            balanceView.setVisibility(View.GONE);
        }
    }

    private SpannableStringBuilder spannableString(String yoBalanceString) {
        final SpannableStringBuilder text = new SpannableStringBuilder(yoBalanceString);
        // Span to make text bold
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
        text.setSpan(new ForegroundColorSpan(Color.RED), 17, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text;
    }
}
