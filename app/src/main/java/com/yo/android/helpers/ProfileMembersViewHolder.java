package com.yo.android.helpers;

import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 7/26/2016.
 */

public class ProfileMembersViewHolder extends AbstractViewHolder {

    private TextView contactNumber;
    private TextView contactMail;
    private TextView permission;
    private TextView tvName;

    public ProfileMembersViewHolder(View view) {
        super(view);
        contactMail = (TextView) view.findViewById(R.id.tv_contact_email);
        contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);
        permission = (TextView) view.findViewById(R.id.isadmin);
        tvName = (TextView)view.findViewById(R.id.tv_name);
    }

    public TextView getContactNumber() {
        return contactNumber;
    }
    public TextView getName() {
        return tvName;
    }

    public TextView getContactMail() {
        return contactMail;
    }

    public TextView getPermission() {
        return permission;
    }
}
