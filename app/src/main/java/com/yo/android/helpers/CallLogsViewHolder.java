package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;


import butterknife.BindView;
import butterknife.ButterKnife;

public class CallLogsViewHolder extends AbstractViewHolder {

    @BindView(R.id.tv_phone_number)
    TextView opponentName;
    @BindView(R.id.header)
    TextView header;
/*    @BindView(R.id.tv_time_stamp)
    TextView timeStamp;*/
    @BindView(R.id.tv_date_time)
    TextView dateTimeStamp;
    @BindView(R.id.iv_message_type)
    TextView messageIcon;
    @BindView(R.id.iv_contact_type)
    ImageView callIcon;
    @BindView(R.id.imv_contact_pic)
    ImageView contactPic;
    @BindView(R.id.row_container)
    View rowContainer;
    @BindView(R.id.row_container_details)
    View rowContainerdetails;
    @BindView(R.id.create_new_contact)
    TextView creatNewContact;
    @BindView(R.id.add_to_contact)
    TextView addToContact;
    @BindView(R.id.info)
    TextView info;
    @BindView(R.id.call_layout)
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

    /*public TextView getTimeStamp() {
        return timeStamp;
    }*/

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
