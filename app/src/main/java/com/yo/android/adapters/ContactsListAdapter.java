package com.yo.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

//import com.squareup.picasso.Picasso;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.model.Contact;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

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

        holder.getContactNumber().setText(item.getName());

        if ((item.getName() != null) && (!item.getName().replaceAll("\\s+", "").equalsIgnoreCase(item.getPhoneNo().trim()))) {
            holder.getContactMail().setText(item.getPhoneNo());
        } else {
            holder.getContactMail().setText("");
        }

        if (!TextUtils.isEmpty(item.getImage())) {

            Glide.with(mContext)
                    .load(item.getImage())
                    .fitCenter()
                    .placeholder(R.drawable.ic_contacts)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.getContactPic());
        } else {

            Drawable drawable = mDrawableBuilder.build(String.valueOf(item.getName().charAt(0)), mColorGenerator.getRandomColor());
            holder.getContactPic().setImageDrawable(drawable);

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
        } else {
            holder.getMessageView().setImageResource(R.drawable.ic_invitefriends);
        }

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
