package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class AppRegisteredContactsViewHolder extends AbstractViewHolder{

    private TextView contactNumber;
    private TextView contactName;
    private ImageView contactPic;
    private ImageView inviteContact;

    public AppRegisteredContactsViewHolder(View view) {
        super(view);
        contactName = (TextView) view.findViewById(R.id.tv_name);
        contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);
        contactPic = (ImageView) view.findViewById(R.id.imv_contact_pic);
        inviteContact = (ImageView) view.findViewById(R.id.iv_invite);
    }

    public TextView getContactNumber() {
        return contactNumber;
    }

    public TextView getContactName() {
        return contactName;
    }

    public ImageView getContactPic() {
        return contactPic;
    }

    public ImageView getInviteContact() {
        return inviteContact;
    }
}
