package com.yo.android.helpers;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 7/18/2016.
 */

public class GroupContactsViewHolder extends AbstractViewHolder {

    private TextView contactNumber;
    private CheckBox checkBox;
    private TextView contactMail;


    public GroupContactsViewHolder(View view) {
        super(view);

        contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);
        checkBox = (CheckBox) view.findViewById(R.id.checkBox);
    }

    public TextView getContactNumber() {
        return contactNumber;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

}
