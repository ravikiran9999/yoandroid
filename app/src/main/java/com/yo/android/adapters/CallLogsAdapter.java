package com.yo.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.calllogs.CallLog;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.CallLogsViewHolder;
import com.yo.android.helpers.Helper;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.ui.CallLogDetailsActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.DateUtil;
import com.yo.android.util.Util;
import com.yo.android.voip.OutGoingCallActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallLogsAdapter extends AbstractBaseAdapter<Map.Entry<String, List<CallLogsResult>>, CallLogsViewHolder> {

    private final PreferenceEndPoint mPrefs;
    private ContactsSyncManager contactsSyncManager;
    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private static int updateViewPosition = -1;
    Context mContext;

    protected DateFormat dateFormat1 = new SimpleDateFormat(DateUtil.DATE_FORMAT9);
    protected DateFormat dateFormat2 = new SimpleDateFormat("hh:mm a");
    private SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected SimpleDateFormat formatterNewDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CallLogsAdapter(Context context, PreferenceEndPoint prefs, ContactsSyncManager contactsSyncManager) {
        super(context);
        this.mPrefs = prefs;
        this.contactsSyncManager = contactsSyncManager;
        // mDrawableBuilder = TextDrawable.builder().rect();
        mDrawableBuilder = TextDrawable.builder().round();
        mContext = context;
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
        Drawable drawable = null;

        String destination_name = item.getValue().get(0).getDestination_name();

        try {
            if (item.getValue().get(0).getAppOrPstn() == CallLog.Calls.APP_TO_APP_CALL) {
                holder.getCallIcon().setImageResource(R.drawable.yo_call_free);
            } else {
                holder.getCallIcon().setImageResource(R.drawable.ic_receiver);
            }
        } catch (Exception e) {

        }
        String formattedString = destination_name;

        holder.getInfo().setVisibility(View.VISIBLE);
        holder.getMessageIcon().setVisibility(View.VISIBLE);
        if (destination_name != null && destination_name.length() >= 1) {

            String numericValue = Util.numericValueFromString(mContext, formattedString);
            holder.getOpponentName().setText(numericValue);

            String title = String.valueOf(destination_name.charAt(0)).toUpperCase();
            Pattern p = Pattern.compile("^[a-zA-Z]");
            Matcher m = p.matcher(title);
            boolean b = m.matches();
            if (b) {
                /*if (holder.getContactPic().getTag(Settings.imageTag) != null && (Drawable) holder.getContactPic().getTag(Settings.imageTag) != null) {
                    holder.getContactPic().setImageDrawable((Drawable) holder.getContactPic().getTag(Settings.imageTag));
                } else {*/
                if (item.getValue().get(0).getDialnumber() != null) {
                    drawable = mDrawableBuilder.build(title, mColorGenerator.getColor(item.getValue().get(0).getDialnumber()));
                } else {
                    drawable = mDrawableBuilder.build(title, mColorGenerator.getColor(item.getValue().get(0).getAppOrPstn()));
                }
                holder.getContactPic().setTag(Settings.imageTag, drawable);
                holder.getContactPic().setImageDrawable(drawable);
                //}
            } else {

                Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
                LayerDrawable bgDrawable = (LayerDrawable) tempImage;
                final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
                if (Settings.isTitlePicEnabled) {
                    if (item.getValue().get(0).getDialnumber() != null) {
                        shape.setColor(mColorGenerator.getColor(item.getValue().get(0).getDialnumber()));
                    } else {
                        shape.setColor(mColorGenerator.getColor(item.getValue().get(0).getAppOrPstn()));
                    }
                }
                drawable = tempImage;
                holder.getContactPic().setImageDrawable(drawable);
                holder.getContactPic().setTag(Settings.imageTag, drawable);


            }
            holder.getCreatNewContact().setVisibility(View.GONE);
            holder.getAddToContact().setVisibility(View.GONE);
        } else {
            Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
            LayerDrawable bgDrawable = (LayerDrawable) tempImage;
            final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);

            String phoneNumber = item.getValue().get(0).getDialnumber();
            if (phoneNumber != null && phoneNumber.contains(Constants.YO_USER)) {
                try {
                    phoneNumber = phoneNumber.substring(phoneNumber.indexOf(Constants.YO_USER) + 6, phoneNumber.length() - 1);
                    holder.getOpponentName().setText(phoneNumber);
                } catch (StringIndexOutOfBoundsException e) {
                }
            } else if (phoneNumber != null) {
                holder.getOpponentName().setText(phoneNumber);
            }
            if (Settings.isTitlePicEnabled && phoneNumber != null) {
                shape.setColor(mColorGenerator.getColor(phoneNumber));
            }
            holder.getContactPic().setImageDrawable(tempImage);
            holder.getContactPic().setTag(Settings.imageTag, tempImage);

            holder.getCreatNewContact().setVisibility(View.VISIBLE);
            holder.getAddToContact().setVisibility(View.VISIBLE);
        }
        if (item.getValue().get(0).getImage() != null) {
            Glide.with(mContext).load(item.getValue().get(0).getImage())
                    .placeholder(drawable)
                    .dontAnimate()
                    .error(drawable).
                    into(holder.getContactPic());
        }

        //By default set these properties
        holder.getHeader().setVisibility(View.GONE);
        holder.getRowContainerdetails().setVisibility(View.GONE);
        holder.getRowContainer().setVisibility(View.VISIBLE);
        if (position == updateViewPosition) {
            holder.getRowContainerdetails().setVisibility(View.VISIBLE);
        } else {
            holder.getRowContainerdetails().setVisibility(View.GONE);
        }
        if (item.getValue().get(0).getAppOrPstn() == CallLog.Calls.APP_TO_PSTN_CALL) {

            holder.getMessageIcon().setVisibility(View.GONE);
        } else {
            holder.getMessageIcon().setVisibility(View.VISIBLE);
        }
        int numberOfCallPerDay = item.getValue().size();
        String numberOfCallsPerDay = "";
        if (numberOfCallPerDay > 1) {
            numberOfCallsPerDay = "(" + item.getValue().size() + ") ";
        }
        if (item.getValue().get(0).isHeader()) {
            holder.getHeader().setVisibility(View.VISIBLE);
            holder.getRowContainerdetails().setVisibility(View.GONE);
            holder.getRowContainer().setVisibility(View.GONE);
            holder.getHeader().setText(item.getValue().get(0).getHeaderTitle());
        } else if (item.getValue().get(0).getCallType() == CallLog.Calls.MISSED_TYPE) {
            holder.getDateTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_holo_dark, 0, 0, 0);
            //holder.getDateTimeStamp().setText(numberOfCallsPerDay.concat(DateUtil.parseConvertUtcToGmt(item.getValue().get(0).getStime())));
            holder.getDateTimeStamp().setText(numberOfCallsPerDay.concat(currentDate(item.getValue().get(0).getStime())));
        } else if (item.getValue().get(0).getCallType() == CallLog.Calls.INCOMING_TYPE) {
            holder.getDateTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_incoming_holo_dark, 0, 0, 0);
            holder.getDateTimeStamp().setText(numberOfCallsPerDay.concat(currentDate(item.getValue().get(0).getStime())));
        } else if (item.getValue().get(0).getCallType() == CallLog.Calls.OUTGOING_TYPE) {
            holder.getDateTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_outgoing_holo_dark, 0, 0, 0);
            holder.getDateTimeStamp().setText(numberOfCallsPerDay.concat(currentDate(item.getValue().get(0).getStime())));
        } else if (item.getValue().get(0).getDialedstatus().equalsIgnoreCase("NOT ANSWER")) {
            holder.getDateTimeStamp().setText(numberOfCallsPerDay.concat(currentDate(item.getValue().get(0).getStime())));
            holder.getDateTimeStamp().setText(numberOfCallsPerDay.concat(currentDate(item.getValue().get(0).getStime())));
        } else if (item.getValue().get(0).getDialedstatus()!=null && item.getValue().get(0).getDialedstatus().equalsIgnoreCase("NOT ANSWER")) {
            holder.getDateTimeStamp().setText(numberOfCallsPerDay.concat(currentDate(item.getValue().get(0).getStime())));
            holder.getDateTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_redarrowdown, 0, 0, 0);
        } else {
            holder.getDateTimeStamp().setText(numberOfCallsPerDay.concat(currentDate(item.getValue().get(0).getStime())));
            holder.getDateTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_greenarrowup, 0, 0, 0);
        }
        //holder.getDateTimeStamp().setText(currentDate(item.getValue().get(0).getStime()));
        //holder.getCallIcon().setTag(item);
        holder.getCallLayout().setTag(item);
        holder.getMessageIcon().setTag(item);
        //holder.getCallIcon().setOnClickListener(new View.OnClickListener() {
        holder.getCallLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map.Entry<String, List<CallLogsResult>> item = (Map.Entry<String, List<CallLogsResult>>) v.getTag();
                boolean isPSTN = item.getValue().get(0).getAppOrPstn() == CallLog.Calls.APP_TO_PSTN_CALL;
                SipHelper.makeCall(mContext, item.getValue().get(0).getDialnumber(), isPSTN);
            }
        });
        holder.getMessageIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map.Entry<String, List<CallLogsResult>> item = (Map.Entry<String, List<CallLogsResult>>) v.getTag();
                Intent intent = new Intent(mContext, ChatActivity.class);
                Contact contact = contactsSyncManager.getContactByVoxUserName(item.getValue().get(0).getDialnumber());
                if (contact == null) {
                    contact = new Contact();
                    contact.setPhoneNo(item.getValue().get(0).getDialnumber());
                    contact.setNexgieUserName(item.getValue().get(0).getDialnumber());
                    contact.setImage(item.getValue().get(0).getImage());
                }
                intent.putExtra(Constants.CONTACT, contact);
                intent.putExtra(Constants.TYPE, Constants.CONTACT);
                mContext.startActivity(intent);
            }
        });
        holder.getInfo().setTag(item);
        holder.getInfo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map.Entry<String, List<CallLogsResult>> callLogDetails = (Map.Entry<String, List<CallLogsResult>>) v.getTag();
                Intent intent = new Intent(mContext, CallLogDetailsActivity.class);
                intent.putParcelableArrayListExtra(Constants.CALL_LOG_DETAILS, (ArrayList<? extends Parcelable>) callLogDetails.getValue());
                mContext.startActivity(intent);
            }
        });
        holder.getCreatNewContact().setTag(item);
        holder.getCreatNewContact().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map.Entry<String, List<CallLogsResult>> callLogDetails = (Map.Entry<String, List<CallLogsResult>>) v.getTag();
                Helper.createNewContactWithPhoneNumber((Activity) mContext, callLogDetails.getValue().get(0).getDialnumber());
            }
        });
        holder.getAddToContact().setTag(item);
        holder.getAddToContact().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map.Entry<String, List<CallLogsResult>> callLogDetails = (Map.Entry<String, List<CallLogsResult>>) v.getTag();
                Helper.addContactWithPhoneNumber((Activity) mContext, callLogDetails.getValue().get(0).getDialnumber());
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

    public void showView(int position) {
        updateViewPosition = position;
        notifyDataSetChanged();
    }

    private void loadAvatarImage(ImageView holder) {
        Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getRandomColor());
        }
        holder.setVisibility(View.VISIBLE);
        if (holder.getTag(Settings.imageTag) == null) {
            holder.setTag(Settings.imageTag, tempImage);
        }
        holder.setImageDrawable(tempImage);
    }

    private String currentDate(@NonNull String mTime) {
        String mDate = null;
        try {
            if (mTime != null) {
                //String day = dateFormat2.format(formatterDate.parse(mTime));
                String day = dateFormat2.format(formatterNewDate.parse(mTime));
                String currentDate = DateUtil.getChatListTimeFormat(convertDateFormatLong(mTime));
                if (currentDate.equalsIgnoreCase(Constants.TODAY) || currentDate.equalsIgnoreCase(Constants.YESTERDAY)) {
                    mDate = currentDate.concat(",").concat(" " + day);
                } else {
                    //mDate = dateFormat1.format(formatterDate.parse(mTime)).concat(",").concat(" " + day);
                    mDate = dateFormat1.format(formatterNewDate.parse(mTime)).concat(",").concat(" " + day);
                }
            } else {
                return mDate;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mDate;
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
