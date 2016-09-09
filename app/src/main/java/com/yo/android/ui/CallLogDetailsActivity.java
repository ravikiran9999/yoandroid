package com.yo.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.BinderThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.sql.Date;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by rajesh on 6/9/16.
 */
public class CallLogDetailsActivity extends BaseActivity {

    @Bind(R.id.imv_calllog_details_profile_pic)
    CircleImageView imageView;

    @Bind(R.id.call_log_opponent_name)
    TextView opponentName;

    @Bind(R.id.call_info_date)
    TextView callInfoDate;

    @Bind(R.id.call)
    ImageView callImg;

    @Bind(R.id.lv_call_log_details)
    ListView callLogHistoryListview;
    protected DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
    protected SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private ArrayList<CallLogsResult> callLogsDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calllog_details);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.call_info);
        callLogsDetails = getIntent().getParcelableArrayListExtra(Constants.CALL_LOG_DETAILS);
        Log.e("", callLogsDetails + "");
        if (callLogsDetails.size() >= 1) {
            Glide.with(this).load(callLogsDetails.get(0).getImage())
                    .placeholder(R.drawable.ic_contacts)
                    .dontAnimate()
                    .error(R.drawable.ic_contacts).
                    into(imageView);
            if (callLogsDetails.get(0).getDestination_name() != null) {
                opponentName.setText(callLogsDetails.get(0).getDestination_name());
            } else {
                opponentName.setText(callLogsDetails.get(0).getDialnumber());
            }
            CallLogDetailsAdapter adapter = new CallLogDetailsAdapter(this, callLogsDetails);
            callLogHistoryListview.setAdapter(adapter);
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM");
            String s = "";
            try {
                s = formatter.format(formatterDate.parse(callLogsDetails.get(0).getStime()));
            } catch (ParseException e) {
            }
            callInfoDate.setText(s);
            callImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SipHelper.makeCall(CallLogDetailsActivity.this, callLogsDetails.get(0).getDialnumber());
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.call_log_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (callLogsDetails.size() >= 1 && item.getItemId() == R.id.menu_delete) {
            CallLog.Calls.deleteCallLogByDate(this, callLogsDetails.get(0).getStime(), callLogsDetails.get(0).getDialnumber());
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class CallLogDetailsAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<CallLogsResult> calllist;
        private LayoutInflater inflater;

        public CallLogDetailsAdapter(Context context, ArrayList<CallLogsResult> list) {
            this.context = context;
            this.calllist = list;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return calllist.size();
        }

        @Override
        public CallLogsResult getItem(int position) {
            return calllist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CallHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.call_log_details_list_item, null);
                holder = new CallHolder();
                holder.callTypeText = (TextView) convertView.findViewById(R.id.calltype);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.duration = (TextView) convertView.findViewById(R.id.duration);
                convertView.setTag(holder);
            } else {
                holder = (CallHolder) convertView.getTag();
            }
            CallLogsResult item = getItem(position);
            try {
                holder.time.setText(dateFormat.format(formatterDate.parse(item.getStime())));
              //  holder.duration.setText(convertMillisToHMmSs(Long.valueOf(item.getDuration())));
            } catch (ParseException e) {
                mLog.w("", e);
            }
            if (item.getCallType() == CallLog.Calls.MISSED_TYPE) {
                holder.callTypeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_holo_dark, 0, 0, 0);
                holder.callTypeText.setText(context.getResources().getString(R.string.missed_call));
            } else if (item.getCallType() == CallLog.Calls.INCOMING_TYPE) {
                holder.callTypeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_incoming_holo_dark, 0, 0, 0);
                holder.callTypeText.setText(context.getResources().getString(R.string.incoming_call));
            } else if (item.getCallType() == CallLog.Calls.OUTGOING_TYPE) {
                holder.callTypeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_outgoing_holo_dark, 0, 0, 0);
                holder.callTypeText.setText(context.getResources().getString(R.string.outgoing_call));
            }
            return convertView;

        }



        public class CallHolder {
            public TextView callTypeText;
            public TextView duration;
            public TextView time;
        }
    }
}
