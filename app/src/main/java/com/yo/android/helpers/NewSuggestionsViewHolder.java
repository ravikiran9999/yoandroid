package com.yo.android.helpers;

import android.view.View;
import android.widget.Button;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by creatives on 9/6/2016.
 */
public class NewSuggestionsViewHolder extends AbstractViewHolder {

    private Button btnTopics;
    private Button btnFollow;

    public NewSuggestionsViewHolder(View view) {
        super(view);
        btnTopics = (Button) view.findViewById(R.id.btn_topics);
        btnFollow = (Button) view.findViewById(R.id.imv_magazine_follow);
    }

    public Button getBtnTopics() {
        return btnTopics;
    }

    public void setBtnTopics(Button btnTopics) {
        this.btnTopics = btnTopics;
    }

    public Button getBtnFollow() {
        return btnFollow;
    }

    public void setBtnFollow(Button btnFollow) {
        this.btnFollow = btnFollow;
    }
}
