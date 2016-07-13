package com.yo.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.di.Injector;
import com.yo.android.helpers.ChatRoomViewHolder;
import com.yo.android.model.ChatRoom;
import com.yo.android.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by rdoddapaneni on 7/5/2016.
 */

public class ChatRoomListAdapter extends AbstractBaseAdapter<ChatRoom, ChatRoomViewHolder> {

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    private DatabaseReference roomReference;


    public ChatRoomListAdapter(Context context) {
        super(context);
        Injector.obtain(context.getApplicationContext()).inject(this);

        roomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM_ID);
    }

    @Override
    public int getLayoutId() {
        return R.layout.chat_room_list_item;
    }

    @Override
    public ChatRoomViewHolder getViewHolder(View convertView) {
        return new ChatRoomViewHolder(convertView);
    }

    @Override
    public void bindView(int position, ChatRoomViewHolder holder, final ChatRoom item) {


        String yourPhoneNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);

        if (item.getOpponentPhoneNumber().equals(yourPhoneNumber)) {
            holder.getOpponentName().setText(item.getYourPhoneNumber());
        } else {
            holder.getOpponentName().setText(item.getOpponentPhoneNumber());
        }

        if (item.isImage()) {
            holder.getChat().setText(mContext.getResources().getString(R.string.image));
            holder.getChat().setTextColor(mContext.getResources().getColor(R.color.dialpad_icon_tint));
        } else if (!TextUtils.isEmpty(item.getMessage())) {
            holder.getChat().setText(item.getMessage());
            holder.getChat().setTextColor(mContext.getResources().getColor(R.color.dialpad_digits_text_color));
        }
        holder.getTimeStamp().setText(item.getTimeStamp());
    }
}
