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
import com.yo.android.util.Util;
import com.yo.android.voip.OutGoingCallActivity;

import java.util.ArrayList;
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

    public CallLogsAdapter(Context context, PreferenceEndPoint prefs, ContactsSyncManager contactsSyncManager) {
        super(context);
        this.mPrefs = prefs;
        this.contactsSyncManager = contactsSyncManager;
        // mDrawableBuilder = TextDrawable.builder().rect();
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
        Drawable drawable = null;
        String destination_name = item.getValue().get(0).getDestination_name();
        holder.getInfo().setVisibility(View.VISIBLE);
        holder.getMessageIcon().setVisibility(View.VISIBLE);
        if (destination_name != null && destination_name.length() >= 1) {
            holder.getOpponentName().setText(destination_name);
            String title = String.valueOf(destination_name.charAt(0)).toUpperCase();
            Pattern p = Pattern.compile("^[a-zA-Z]");
            Matcher m = p.matcher(title);
            boolean b = m.matches();
            if (b) {
                drawable = mDrawableBuilder.build(title, mColorGenerator.getRandomColor());
                holder.getContactPic().setImageDrawable(drawable);
            } else {
                Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
                LayerDrawable bgDrawable = (LayerDrawable) tempImage;
                final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
                if (Settings.isTitlePicEnabled) {
                    shape.setColor(mColorGenerator.getRandomColor());
                }
                drawable = tempImage;            }
            holder.getCreatNewContact().setVisibility(View.GONE);
            holder.getAddToContact().setVisibility(View.GONE);
        } else {
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
            Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
            LayerDrawable bgDrawable = (LayerDrawable) tempImage;
            final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
            if (Settings.isTitlePicEnabled) {
                shape.setColor(mColorGenerator.getRandomColor());
            }
            drawable = tempImage;
            holder.getCreatNewContact().setVisibility(View.VISIBLE);
            holder.getAddToContact().setVisibility(View.VISIBLE);
        }
        if (item.getValue().get(0).getImage() != null) {
            Glide.with(mContext).load(item.getValue().get(0).getImage())
                    .placeholder(drawable)
                    .dontAnimate()
                    .error(drawable).
                    into(holder.getContactPic());
        } else if (Settings.isTitlePicEnabled) {
            if (holder.getContactPic().getTag(Settings.imageTag) == null) {
                holder.getContactPic().setTag(Settings.imageTag, drawable);
            }
            holder.getContactPic().setImageDrawable((Drawable) holder.getContactPic().getTag(Settings.imageTag));
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
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed_holo_dark, 0, 0, 0);
            holder.getTimeStamp().setText(numberOfCallsPerDay + Util.parseConvertUtcToGmt(item.getValue().get(0).getStime()));
        } else if (item.getValue().get(0).getCallType() == CallLog.Calls.INCOMING_TYPE) {
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_incoming_holo_dark, 0, 0, 0);
            holder.getTimeStamp().setText(numberOfCallsPerDay + Util.parseConvertUtcToGmt(item.getValue().get(0).getStime()));
        } else if (item.getValue().get(0).getCallType() == CallLog.Calls.OUTGOING_TYPE) {
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_outgoing_holo_dark, 0, 0, 0);
            holder.getTimeStamp().setText(numberOfCallsPerDay + Util.parseConvertUtcToGmt(item.getValue().get(0).getStime()));
        } else if (item.getValue().get(0).getDialedstatus().equalsIgnoreCase("NOT ANSWER")) {
            holder.getTimeStamp().setText(numberOfCallsPerDay + Util.parseConvertUtcToGmt(item.getValue().get(0).getStime()));
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_redarrowdown, 0, 0, 0);
        } else {
            holder.getTimeStamp().setText(numberOfCallsPerDay + Util.parseConvertUtcToGmt(item.getValue().get(0).getStime()));
            holder.getTimeStamp().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_greenarrowup, 0, 0, 0);
        }
        holder.getCallIcon().setTag(item);
        holder.getMessageIcon().setTag(item);
        holder.getCallIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map.Entry<String, List<CallLogsResult>> item = (Map.Entry<String, List<CallLogsResult>>) v.getTag();
                SipHelper.makeCall(mContext, item.getValue().get(0).getDialnumber());
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
                    contact.setVoxUserName(item.getValue().get(0).getDialnumber());
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
}
