package com.yo.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.DateUtil;
import com.yo.android.util.Util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    @Bind(R.id.call_log_opponent_number)
    TextView opponentNumber;

    @Bind(R.id.call_info_date)
    TextView callInfoDate;

    @Bind(R.id.call)
    ImageView callImg;

    @Bind(R.id.lv_call_log_details)
    ListView callLogHistoryListview;
    protected DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
    protected DateFormat dateFormat1 = new SimpleDateFormat("MM/dd");
    protected DateFormat dateFormat2 = new SimpleDateFormat("EEE");
    protected SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected SimpleDateFormat formatterNewDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ArrayList<CallLogsResult> callLogsDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calllog_details);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.call_info);
        callLogsDetails = getIntent().getParcelableArrayListExtra(Constants.CALL_LOG_DETAILS);
        String name = callLogsDetails.get(0).getDestination_name();
        String number = callLogsDetails.get(0).getDialnumber();
        if (number != null && number.contains(Constants.YO_USER)) {
            number = number.replaceAll("[^\\d.]", "").substring(2, 12);
        }
        Log.e("", callLogsDetails + "");
        if (callLogsDetails.size() >= 1) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.dynamic_profile)
                    .dontAnimate()
                    .error(R.drawable.dynamic_profile);
            Glide.with(this).load(callLogsDetails.get(0).getImage())
                    .apply(requestOptions)
                    .into(imageView);
            if (!TextUtils.isEmpty(name)) {
                opponentName.setVisibility(View.VISIBLE);
                opponentName.setText(name);
                if (number != null && number.contains(Constants.YO_USER)) {
                    try {
                        number = number.substring(number.indexOf(Constants.YO_USER) + 6, number.length() - 1);

                    } catch (StringIndexOutOfBoundsException e) {
                    }
                }
                opponentNumber.setText(number);
            } else if (!TextUtils.isEmpty(number)) {
                opponentName.setVisibility(View.GONE);
                if (number != null && number.contains(Constants.YO_USER)) {
                    try {
                        number = number.substring(number.indexOf(Constants.YO_USER) + 6, number.length() - 1);

                    } catch (StringIndexOutOfBoundsException e) {
                    }
                }
                opponentNumber.setText(number);
            }
            CallLogDetailsAdapter adapter = new CallLogDetailsAdapter(this, callLogsDetails);
            callLogHistoryListview.setAdapter(adapter);
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM");
            String s = "";
            try {
                s = formatter.format(formatterDate.parse(callLogsDetails.get(0).getStime()));
            } catch (ParseException e) {
            }
            //callInfoDate.setText(s);
            callImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isPSTN =callLogsDetails.get(0).getAppOrPstn() == CallLog.Calls.APP_TO_PSTN_CALL ? true : false;
                    SipHelper.makeCall(CallLogDetailsActivity.this, callLogsDetails.get(0).getDialnumber(),isPSTN);
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
                holder.ioIcon = (ImageView) convertView.findViewById(R.id.io_icon);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                convertView.setTag(holder);
            } else {
                holder = (CallHolder) convertView.getTag();
            }
            CallLogsResult item = getItem(position);
            try {
                String mDate = null;
                String day = dateFormat2.format(formatterDate.parse(item.getStime()));
                String currentDate = DateUtil.getChatListTimeFormat(convertDateFormatLong(item.getStime()));
                if (currentDate.equalsIgnoreCase(Constants.TODAY) || currentDate.equalsIgnoreCase(Constants.YESTERDAY)) {
                    mDate = currentDate;
                } else {
                    mDate = dateFormat1.format(formatterDate.parse(item.getStime())).concat(",").concat(" " + day);
                }
                holder.date.setText(mDate);
                //holder.time.setText(dateFormat.format(formatterDate.parse(item.getStime())));
                holder.time.setText(dateFormat.format(formatterNewDate.parse(item.getStime())));
                holder.duration.setText(Util.convertSecToHMmSs(Long.valueOf(item.getDuration())));
            } catch (ParseException e) {
                mLog.w("", e);
            }
            if (item.getCallType() == CallLog.Calls.MISSED_TYPE) {
                holder.ioIcon.setImageResource(R.drawable.ic_call_missed_holo_dark);
                holder.callTypeText.setText(context.getResources().getString(R.string.missed_call));
            } else if (item.getCallType() == CallLog.Calls.INCOMING_TYPE) {
                holder.ioIcon.setImageResource(R.drawable.ic_call_incoming_holo_dark);
                holder.callTypeText.setText(context.getResources().getString(R.string.incoming_call));
            } else if (item.getCallType() == CallLog.Calls.OUTGOING_TYPE) {
                holder.ioIcon.setImageResource(R.drawable.ic_call_outgoing_holo_dark);
                holder.callTypeText.setText(context.getResources().getString(R.string.outgoing_call));
            }
            return convertView;

        }

        public class CallHolder {
            TextView callTypeText;
            public TextView duration;
            public TextView time;
            ImageView ioIcon;
            TextView date;
        }

        private long convertDateFormatLong(String dateString) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(dateString);
                return date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }
}
