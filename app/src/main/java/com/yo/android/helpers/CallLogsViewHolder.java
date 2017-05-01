package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CallLogsViewHolder extends AbstractViewHolder {

    @Bind(R.id.tv_phone_number)
    TextView opponentName;
    @Bind(R.id.header)
    TextView header;
    @Bind(R.id.tv_time_stamp)
    TextView timeStamp;
    @Bind(R.id.tv_date_time)
    TextView dateTimeStamp;
    @Bind(R.id.iv_message_type)
    TextView messageIcon;
    @Bind(R.id.iv_contact_type)
    ImageView callIcon;
    @Bind(R.id.imv_contact_pic)
    ImageView contactPic;
    @Bind(R.id.row_container)
    View rowContainer;
    @Bind(R.id.row_container_details)
    View rowContainerdetails;
    @Bind(R.id.create_new_contact)
    TextView creatNewContact;
    @Bind(R.id.add_to_contact)
    TextView addToContact;
    @Bind(R.id.info)
    TextView info;
    @Bind(R.id.call_layout)
    View callLayout;

    public TextView getHeader() {
        return header;
    }
    public TextView getCreatNewContact() {
        return creatNewContact;
    }

    public void setCreatNewContact(TextView creatNewContact) {
        this.creatNewContact = creatNewContact;
    }

    public TextView getAddToContact() {
        return addToContact;
    }

    public void setAddToContact(TextView addToContact) {
        this.addToContact = addToContact;
    }

    public TextView getInfo() {
        return info;
    }

    public void setInfo(TextView info) {
        this.info = info;
    }


    public View getRowContainerdetails() {
        return rowContainerdetails;
    }

    public void setRowContainerdetails(View rowContainerdetails) {
        this.rowContainerdetails = rowContainerdetails;
    }


    public View getRowContainer() {
        return rowContainer;
    }


    public CallLogsViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public TextView getMessageIcon() {
        return messageIcon;
    }

    public TextView getOpponentName() {
        return opponentName;
    }

    public TextView getTimeStamp() {
        return timeStamp;
    }

    public ImageView getCallIcon() {
        return callIcon;
    }

    public ImageView getContactPic() {
        return contactPic;
    }

    public TextView getDateTimeStamp() {
        return dateTimeStamp;
    }

    public View getCallLayout() { return callLayout; }
}
