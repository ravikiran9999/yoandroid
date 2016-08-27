package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
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

    private ImageView messageIcon;

    private ImageView callIcon;
    private ImageView contactPic;

    public View getRowContainer() {
        return rowContainer;
    }

    private View rowContainer;

    public CallLogsViewHolder(View view) {
        super(view);
        opponentName = (TextView) view.findViewById(R.id.tv_phone_number);
        header = (TextView) view.findViewById(R.id.header);
        rowContainer = view.findViewById(R.id.row_container);
        timeStamp = (TextView) view.findViewById(R.id.tv_time_stamp);
        messageIcon = (ImageView) view.findViewById(R.id.iv_message_type);
        callIcon = (ImageView) view.findViewById(R.id.iv_contact_type);
        contactPic = (ImageView)view.findViewById(R.id.imv_contact_pic);
    }

    public ImageView getMessageIcon() {
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
}
