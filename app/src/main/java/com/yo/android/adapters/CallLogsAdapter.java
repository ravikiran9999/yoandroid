package com.yo.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.CallLogsViewHolder;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.voip.OutGoingCallActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CallLogsAdapter extends AbstractBaseAdapter<Map.Entry<String, List<CallLogsResult>>, CallLogsViewHolder> {

    private final PreferenceEndPoint mPrefs;
    private ContactsSyncManager contactsSyncManager;
    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    public CallLogsAdapter(Context context, PreferenceEndPoint prefs,ContactsSyncManager contactsSyncManager) {
        super(context);
        this.mPrefs = prefs;
        this.contactsSyncManager = contactsSyncManager;
        mDrawableBuilder = TextDrawable.builder()
                .round();
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
    public void bindView(int position, CallLogsViewHolder holder, Map.Entry<String, List<CallLogsResult>> item) {
        TextDrawable drawable;
        String destination_name = item.getValue().get(0).getDestination_name();
        if(destination_name !=null) {
            holder.getOpponentName().setText(destination_name);
            drawable = mDrawableBuilder.build(String.valueOf(destination_name.charAt(0)), mColorGenerator.getColor(destination_name));

        }else{
            String phoneNumber =item.getValue().get(0).getDialnumber();
            holder.getOpponentName().setText(phoneNumber);
            drawable = mDrawableBuilder.build(String.valueOf(phoneNumber.charAt(0)), mColorGenerator.getColor(phoneNumber));

        }

        Glide.with(mContext).load(item.getValue().get(0).getImage())
                .placeholder(drawable)
                .dontAnimate()
                .error(drawable).
                into(holder.getContactPic());


        //By default set these properties
        holder.getHeader().setVisibility(View.GONE);
        holder.getRowContainer().setVisibility(View.VISIBLE);
        if(item.getValue().get(0).getDestination_name() == null){
            holder.getMessageIcon().setVisibility(View.GONE);
        }else{
            holder.getMessageIcon().setVisibility(View.VISIBLE);
        }
        int numberOfCallPerDay = item.getValue().size();
        String numberOfCallsPerDay = "";
        if(numberOfCallPerDay >1){
            numberOfCallsPerDay = "("+item.getValue().size()+") ";
        }
        if (item.getValue().get(0).isHeader()) {
            holder.getHeader().setVisibility(View.VISIBLE);
            holder.getRowContainer().setVisibility(View.GONE);
            holder.getHeader().setText(item.getValue().get(0).getHeaderTitle());
        } else if (item.getValue().get(0).getCallType() == CallLog.Calls.MISSED_TYPE) {
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_holo_dark, 0, 0, 0);
            holder.getTimeStamp().setText(numberOfCallsPerDay+Util.parseConvertUtcToGmt(item.getValue().get(0).getStime()));
        }else if(item.getValue().get(0).getCallType() == CallLog.Calls.INCOMING_TYPE){
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_incoming_holo_dark, 0, 0, 0);
            holder.getTimeStamp().setText(numberOfCallsPerDay+Util.parseConvertUtcToGmt(item.getValue().get(0).getStime()));        }
        else if(item.getValue().get(0).getCallType() == CallLog.Calls.OUTGOING_TYPE){
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_outgoing_holo_dark, 0, 0, 0);
            holder.getTimeStamp().setText(numberOfCallsPerDay+Util.parseConvertUtcToGmt(item.getValue().get(0).getStime()));
        }else if (item.getValue().get(0).getDialedstatus().equalsIgnoreCase("NOT ANSWER")) {
            holder.getTimeStamp().setText(numberOfCallsPerDay+Util.parseConvertUtcToGmt(item.getValue().get(0).getStime()));
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_redarrowdown, 0, 0, 0);
        } else {
            holder.getTimeStamp().setText(numberOfCallsPerDay+Util.parseConvertUtcToGmt(item.getValue().get(0).getStime()));
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_greenarrowup, 0, 0, 0);
        }
        holder.getCallIcon().setTag(item);
        holder.getMessageIcon().setTag(item);
        holder.getCallIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map.Entry<String, List<CallLogsResult>> item = (Map.Entry<String, List<CallLogsResult>>)v.getTag();
                SipHelper.makeCall(mContext, item.getValue().get(0).getDialnumber());
            }
        });
        holder.getMessageIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map.Entry<String, List<CallLogsResult>> item = (Map.Entry<String, List<CallLogsResult>>)v.getTag();
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(Constants.CONTACT,contactsSyncManager.getContactByVoxUserName(item.getValue().get(0).getDialnumber()) );
                intent.putExtra(Constants.TYPE, Constants.CONTACT);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    protected boolean hasData(Map.Entry<String, List<CallLogsResult>> event, String key) {
        if (event.getKey() != null && event.getKey().contains(key)) {
            return true;
        }
        return super.hasData(event, key);
    }
}
