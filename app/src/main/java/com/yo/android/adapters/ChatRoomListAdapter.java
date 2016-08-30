package com.yo.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.orion.android.common.preferences.PreferenceEndPoint;
//import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.di.Injector;
import com.yo.android.helpers.ChatRoomViewHolder;
import com.yo.android.model.Room;
import com.yo.android.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by rdoddapaneni on 7/5/2016.
 */

public class ChatRoomListAdapter extends AbstractBaseAdapter<Room, ChatRoomViewHolder> {

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    Context context;

    public ChatRoomListAdapter(Context context) {
        super(context);
        Injector.obtain(context.getApplicationContext()).inject(this);
        this.context = context;
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
    public void bindView(int position, final ChatRoomViewHolder holder, final Room item) {

        String yourPhoneNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);

        if (item.getGroupName() == null) {
            /*for (int i = 0; item.getMembers() != null && i < item.getMembers().size(); i++) {
                if (!item.getMembers().get(i).getMobileNumber().equalsIgnoreCase(yourPhoneNumber)) {
                    holder.getOpponentName().setText(item.getMembers().get(i).getMobileNumber());
                }
            }*/

            holder.getOpponentName().setText(item.getFullName());

            Glide.with(mContext).load(item.getImage())
                    .asBitmap().centerCrop()
                    .placeholder(R.drawable.ic_contactprofile)
                    .error(R.drawable.ic_contactprofile)
                    .into(new BitmapImageViewTarget(holder.getChatRoomPic()) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    holder.getChatRoomPic().setImageDrawable(circularBitmapDrawable);
                }
            });

        } else if (item.getGroupName() != null) {
            holder.getOpponentName().setText(item.getGroupName());
            Glide.with(mContext).load(item.getImage())
                    .placeholder(R.drawable.ic_group)
                    .dontAnimate()
                    .error(R.drawable.ic_group).
                    into(holder.getChatRoomPic());
        } else {
            holder.getOpponentName().setText("");

            Glide.with(context)
                    .load(R.drawable.ic_contactprofile)
                    .fitCenter()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.getChatRoomPic());
        }

        if (item.isImages()) {
            holder.getChat().setText(mContext.getResources().getString(R.string.image));
            holder.getChat().setTextColor(mContext.getResources().getColor(R.color.dialpad_icon_tint));
        } else if (!TextUtils.isEmpty(item.getLastChat())) {
            holder.getChat().setText(item.getLastChat());
            holder.getChat().setTextColor(mContext.getResources().getColor(R.color.dialpad_digits_text_color));
        } else {
            holder.getChat().setText("");
            holder.getChat().setTextColor(mContext.getResources().getColor(R.color.dialpad_digits_text_color));
        }
        holder.getTimeStamp().setText(item.getTimeStamp());
    }
}
