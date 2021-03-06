package com.yo.android.helpers;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 7/18/2016.
 */

public class GroupContactsViewHolder extends AbstractViewHolder {

    private TextView contactNumber;
    private CheckBox checkBox;
    private TextView contactName;
    private ImageView contactPic;

    public GroupContactsViewHolder(View view) {
        super(view);

        contactPic = (ImageView) view.findViewById(R.id.imv_contact_pic);
        contactNumber = (TextView) view.findViewById(R.id.tv_phone_number);
        contactName = (TextView) view.findViewById(R.id.tv_name);
        checkBox = (CheckBox) view.findViewById(R.id.checkBox);
    }

    public TextView getContactNumber() {
        return contactNumber;
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public TextView getContactName() {
        return contactName;
    }

    public ImageView getContactPic() {
        return contactPic;
    }
}
