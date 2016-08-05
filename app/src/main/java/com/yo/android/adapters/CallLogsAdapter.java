package com.yo.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.helpers.CallLogsViewHolder;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.voip.OutGoingCallActivity;

public class CallLogsAdapter extends AbstractBaseAdapter<CallLogsResult, CallLogsViewHolder> {

    private final PreferenceEndPoint mPrefs;

    public CallLogsAdapter(Context context, PreferenceEndPoint prefs) {
        super(context);
        this.mPrefs = prefs;
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
        holder.getOpponentName().setText(item.getDialnumber());
        item.getDialedstatus();//NOT  ANSWER,ANSWER
        //By default set these properties
        holder.getHeader().setVisibility(View.GONE);
        holder.getRowContainer().setVisibility(View.VISIBLE);
        if (item.isHeader()) {
            holder.getHeader().setVisibility(View.VISIBLE);
            holder.getRowContainer().setVisibility(View.GONE);
            holder.getHeader().setText(item.getHeaderTitle());
        } else if (item.getDialedstatus().equalsIgnoreCase("NOT ANSWER")) {
            holder.getTimeStamp().setText(Util.parseConvertUtcToGmt(item.getStime()));
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_redarrowdown, 0, 0, 0);
        } else {
            holder.getTimeStamp().setText(Util.parseConvertUtcToGmt(item.getStime()));
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_greenarrowup, 0, 0, 0);
        }
        holder.getCallIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, OutGoingCallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(OutGoingCallActivity.CALLER_NO, item.getDialnumber());
                mContext.startActivity(intent);
            }
        });
        holder.getMessageIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ContactsListAdapter.showUserChatScreen(mContext, mPrefs.getStringPreference(Constants.PHONE_NUMBER), item.getDialnumber());
            }
        });

    }


}
