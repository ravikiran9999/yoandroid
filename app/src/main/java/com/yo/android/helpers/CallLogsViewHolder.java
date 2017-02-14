package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

public class CallLogsViewHolder extends AbstractViewHolder {

    private TextView opponentName;

    public TextView getHeader() {
        return header;
    }

    private TextView header;
    private TextView timeStamp;
    private TextView dateTimeStamp;

    private TextView messageIcon;

    private ImageView callIcon;
    private ImageView contactPic;
    private View rowContainer;
    private View rowContainerdetails;
    private TextView creatNewContact;
    private TextView addToContact;
    private TextView info;
    private View callLayout;

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
        opponentName = (TextView) view.findViewById(R.id.tv_phone_number);
        header = (TextView) view.findViewById(R.id.header);
        rowContainer = view.findViewById(R.id.row_container);
        timeStamp = (TextView) view.findViewById(R.id.tv_time_stamp);
        timeStamp = (TextView) view.findViewById(R.id.tv_time_stamp);
        messageIcon = (TextView) view.findViewById(R.id.iv_message_type);
        callIcon = (ImageView) view.findViewById(R.id.iv_contact_type);
        contactPic = (ImageView) view.findViewById(R.id.imv_contact_pic);
        rowContainerdetails = view.findViewById(R.id.row_container_details);
        info = (TextView) view.findViewById(R.id.info);
        addToContact = (TextView) view.findViewById(R.id.add_to_contact);
        creatNewContact = (TextView) view.findViewById(R.id.create_new_contact);
        dateTimeStamp = (TextView) view.findViewById(R.id.tv_date_time);
        callLayout = (View) view.findViewById(R.id.call_layout);
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
