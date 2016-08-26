package com.yo.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.CallLogsViewHolder;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.voip.OutGoingCallActivity;

public class CallLogsAdapter extends AbstractBaseAdapter<CallLogsResult, CallLogsViewHolder> {

    private final PreferenceEndPoint mPrefs;
    private ContactsSyncManager contactsSyncManager;

    public CallLogsAdapter(Context context, PreferenceEndPoint prefs,ContactsSyncManager contactsSyncManager) {
        super(context);
        this.mPrefs = prefs;
        this.contactsSyncManager = contactsSyncManager;
    }



    @Override
    public int getLayoutId() {
        return R.layout.dialer_calllogs_list_item;
    }

    @Override
    public CallLogsViewHolder getViewHolder(View convertView) {
        return new CallLogsViewHolder(convertView);
    }

    @Override
    protected boolean hasData(CallLogsResult event, String key) {
        if (event.getDialnumber() != null && event.getDialnumber().contains(key)) {
            return true;
        }
        return super.hasData(event, key);
    }

    @Override
    public void bindView(int position, CallLogsViewHolder holder, final CallLogsResult item) {
        if(item.getDestination_name()!=null) {
            holder.getOpponentName().setText(item.getDestination_name());
        }else{
            holder.getOpponentName().setText(item.getDialnumber());
        }
        item.getDialedstatus();//NOT  ANSWER,ANSWER
        //By default set these properties
        holder.getHeader().setVisibility(View.GONE);
        holder.getRowContainer().setVisibility(View.VISIBLE);
        if(item.getDestination_name() == null){
            holder.getMessageIcon().setVisibility(View.GONE);
        }else{
            holder.getMessageIcon().setVisibility(View.VISIBLE);
        }
        if (item.isHeader()) {
            holder.getHeader().setVisibility(View.VISIBLE);
            holder.getRowContainer().setVisibility(View.GONE);
            holder.getHeader().setText(item.getHeaderTitle());
        } else if (item.getCallType() == CallLog.Calls.MISSED_TYPE) {
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_holo_dark, 0, 0, 0);
            holder.getTimeStamp().setText(Util.parseConvertUtcToGmt(item.getStime()));
        }else if(item.getCallType() == CallLog.Calls.INCOMING_TYPE){
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_incoming_holo_dark, 0, 0, 0);
            holder.getTimeStamp().setText(Util.parseConvertUtcToGmt(item.getStime()));        }
        else if(item.getCallType() == CallLog.Calls.OUTGOING_TYPE){
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_outgoing_holo_dark, 0, 0, 0);
            holder.getTimeStamp().setText(Util.parseConvertUtcToGmt(item.getStime()));
        }else if (item.getDialedstatus().equalsIgnoreCase("NOT ANSWER")) {
            holder.getTimeStamp().setText(Util.parseConvertUtcToGmt(item.getStime()));
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_redarrowdown, 0, 0, 0);
        } else {
            holder.getTimeStamp().setText(Util.parseConvertUtcToGmt(item.getStime()));
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_greenarrowup, 0, 0, 0);
        }
        holder.getCallIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SipHelper.makeCall(mContext, item.getDialnumber());
            }
        });
        holder.getMessageIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(Constants.CONTACT,contactsSyncManager.getContactByVoxUserName(item.getDialnumber()) );
                intent.putExtra(Constants.TYPE, Constants.CONTACT);
                mContext.startActivity(intent);
            }
        });

    }


}
