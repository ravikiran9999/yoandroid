package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class RegisteredContactsViewHolder extends AbstractViewHolder {

    private TextView contactNumber;
    private TextView contactMail;
    private ImageView messageView;
    private ImageView callView;
    private ImageView contactPic;

    public RegisteredContactsViewHolder(View view) {
        super(view);
        contactMail = (TextView) view.findViewById(R.id.tv_contact_email);
        contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);
        messageView = (ImageView) view.findViewById(R.id.iv_message_type);
        callView = (ImageView) view.findViewById(R.id.iv_contact_type);
        contactPic = (ImageView) view.findViewById(R.id.imv_contact_pic);
    }

    public TextView getContactNumber() {
        return contactNumber;
    }

    public TextView getContactMail() {
        return contactMail;
    }

    public ImageView getMessageView() {
        return messageView;
    }

    public ImageView getCallView() {
        return callView;
    }

    public ImageView getContactPic() {
        return contactPic;
    }
}
