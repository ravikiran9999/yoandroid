package com.yo.android.helpers;

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class AppRegisteredContactsViewHolder extends AbstractViewHolder{

    private TextView contactNumber;
    private TextView contactMail;

    public AppRegisteredContactsViewHolder(View view) {
        super(view);
        contactMail = (TextView) view.findViewById(R.id.tv_contact_email);
        contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);

    }

    public TextView getContactNumber() {
        return contactNumber;
    }

    public TextView getContactMail() {
        return contactMail;
    }
}
