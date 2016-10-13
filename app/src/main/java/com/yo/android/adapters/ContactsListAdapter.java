package com.yo.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.View;

//import com.squareup.picasso.Picasso;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class ContactsListAdapter extends AbstractBaseAdapter<Contact, RegisteredContactsViewHolder> {

    private Context context;
    private String userId;
    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    public ContactsListAdapter(Context context, String userId) {
        super(context);
        this.context = context;
        this.userId = userId;
        mDrawableBuilder = TextDrawable.builder()
                .round();

    }

    @Override
    public int getLayoutId() {
        return R.layout.contacts_list_item;
    }

    @Override
    public RegisteredContactsViewHolder getViewHolder(View convertView) {
        return new RegisteredContactsViewHolder(convertView);
    }

    @Override
    public void bindView(final int position, RegisteredContactsViewHolder holder, final Contact item) {

        if (!TextUtils.isEmpty(item.getName())) {

            if (TextUtils.isDigitsOnly(item.getName().replaceAll("\\s+", ""))) {
                String numberWithCountryCode = "+" + item.getCountryCode().concat(item.getPhoneNo());
                holder.getContactNumber().setText(numberWithCountryCode);
            } else {
                holder.getContactNumber().setText(item.getName());
            }
            holder.getContactNumber().setVisibility(View.VISIBLE);
        } else {
            holder.getContactNumber().setVisibility(View.GONE);
        }

        if (item.getPhoneNo() != null) {
            item.setPhoneNo(item.getPhoneNo().trim());
        }

        if ((item.getName() != null) && (!item.getName().replaceAll("\\s+", "").equalsIgnoreCase(item.getPhoneNo()))) {
            String numberWithCountryCode = "+" + item.getCountryCode().concat(item.getPhoneNo());
            holder.getContactMail().setText(numberWithCountryCode);
            holder.getContactMail().setVisibility(View.VISIBLE);

        } else {
            holder.getContactMail().setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(item.getImage())) {

            Glide.with(mContext)
                    .load(item.getImage())
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile)
                    .into(holder.getContactPic());
        } else if (Settings.isTitlePicEnabled) {
            if (item.getName() != null && item.getName().length() >= 1) {
                String title = String.valueOf(item.getName().charAt(0)).toUpperCase();
                Pattern p = Pattern.compile("^[a-zA-Z]");
                Matcher m = p.matcher(title);
                boolean b = m.matches();
                if (b) {
                    Drawable drawable = mDrawableBuilder.build(title, mColorGenerator.getRandomColor());
                    holder.getContactPic().setImageDrawable(drawable);
                } else {
                    loadAvatarImage(holder);
                }
            }
        } else {
            loadAvatarImage(holder);
        }

        //holder.getContactMail().setText(item.getEmailId());


        holder.getMessageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getYoAppUser()) {

                    navigateToChatScreen(item);

                    /*String yourPhoneNumber = userId;
                    String opponentPhoneNumber = item.getPhoneNo();

                    if(item.getFirebaseRoomId() != null) {
                        navigateToChatScreen(mContext, item.getFirebaseRoomId(), opponentPhoneNumber, yourPhoneNumber, null);
                    } else {
                        navigateToChatScreen(mContext, "", opponentPhoneNumber, yourPhoneNumber, item.getId());
                    }*/

                } else {
                    Util.inviteFriend(context, item.getPhoneNo());
                }
            }
        });

        holder.getCallView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Registration registration = getAllItems().get(position);
                //String opponentPhoneNumber = registration.getPhoneNumber();
                String opponentPhoneNumber = item.getVoxUserName();

                if (opponentPhoneNumber != null) {
                    SipHelper.makeCall(mContext, opponentPhoneNumber);
                } else {
                    if (item.getCountryCode() != null && item.getPhoneNo() != null) {
                        SipHelper.makeCall(mContext, item.getCountryCode() + item.getPhoneNo());
                    } else {
                        //TODO: Think about it
                    }
                }
            }
        });

        if (item.getYoAppUser()) {
            holder.getMessageView().setImageResource(R.drawable.ic_message);
            holder.getCallView().setImageResource(R.drawable.yo_call_free);
        } else {
            holder.getMessageView().setImageResource(R.drawable.ic_invitefriends);
            holder.getCallView().setImageResource(R.drawable.ic_receiver);
        }

    }

    private void loadAvatarImage(RegisteredContactsViewHolder holder) {
        Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getRandomColor());
        }
        holder.getContactPic().setImageDrawable(tempImage);
    }

    private static void navigateToChatScreen(Context context, String roomId, String opponentPhoneNumber, String yourPhoneNumber, String opponentId) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.CHAT_ROOM_ID, roomId);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentPhoneNumber);
        intent.putExtra(Constants.OPPONENT_ID, opponentId);
        intent.putExtra(Constants.YOUR_PHONE_NUMBER, yourPhoneNumber);
        context.startActivity(intent);

    }

    private void navigateToChatScreen(Contact contact) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.CONTACT, contact);
        intent.putExtra(Constants.TYPE, Constants.CONTACT);
        context.startActivity(intent);

    }
}
