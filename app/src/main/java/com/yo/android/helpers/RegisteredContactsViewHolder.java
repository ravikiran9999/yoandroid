package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class RegisteredContactsViewHolder extends AbstractViewHolder {

    private TextView contactNumber;
    private TextView contactMail;
    private ImageView messageView;
    private ImageView callView;
    private CircleImageView contactPic;

    public RegisteredContactsViewHolder(View view) {
        super(view);
        contactMail = (TextView) view.findViewById(R.id.tv_contact_email);
        contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);
        messageView = (ImageView) view.findViewById(R.id.iv_message_type);
        callView = (ImageView) view.findViewById(R.id.iv_contact_type);
        contactPic = (CircleImageView) view.findViewById(R.id.imv_contact_pic);
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

    public CircleImageView getContactPic() {
        return contactPic;
    }
}
