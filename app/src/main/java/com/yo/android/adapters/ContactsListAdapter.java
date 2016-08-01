package com.yo.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.model.Contact;
import com.yo.android.util.Constants;
import com.yo.android.voip.OutGoingCallActivity;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class ContactsListAdapter extends AbstractBaseAdapter<Contact, RegisteredContactsViewHolder> {

    private Context context;
    private String userId;

    public ContactsListAdapter(Context context, String userId) {
        super(context);
        this.context = context;
        this.userId = userId;
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
        if (!item.getYoAppUser()) {
            holder.getContactNumber().setText(item.getPhoneNo());
        } else {
            holder.getContactNumber().setText(item.getName());
            holder.getContactMail().setText(item.getPhoneNo());
        }

        if (!TextUtils.isEmpty(item.getImage())) {
            Picasso.with(mContext)
                    .load(item.getImage())
                    .fit()
                    .placeholder(R.drawable.ic_contacts)
                    .error(R.drawable.ic_contacts)
                    .into(holder.getContactPic());
        } else {
            Picasso.with(mContext)
                    .load(R.drawable.ic_contacts)
                    .fit()
                    .placeholder(R.drawable.ic_contacts)
                    .error(R.drawable.ic_contacts)
                    .into(holder.getContactPic());
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
                    Toast.makeText(mContext, "Invite friends need to implement.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.getCallView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Registration registration = getAllItems().get(position);
                //String opponentPhoneNumber = registration.getPhoneNumber();
                String opponentPhoneNumber = item.getPhoneNo();

                if (opponentPhoneNumber != null) {
                    Intent intent = new Intent(context, OutGoingCallActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(OutGoingCallActivity.CALLER_NO, opponentPhoneNumber);
                    context.startActivity(intent);
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
