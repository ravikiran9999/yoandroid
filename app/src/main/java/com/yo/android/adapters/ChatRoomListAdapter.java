package com.yo.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.di.Injector;
import com.yo.android.helpers.ChatRoomViewHolder;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Room;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
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
    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    public ChatRoomListAdapter(Context context) {
        super(context);
        Injector.obtain(context.getApplicationContext()).inject(this);
        this.context = context;
        mDrawableBuilder = TextDrawable.builder()
                .round();
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
            if (TextUtils.isEmpty(item.getFullName())) {
                holder.getOpponentName().setText(item.getMobileNumber());
            } else {
                holder.getOpponentName().setText(item.getFullName());
            }

            Glide.with(mContext).load(item.getImage())
                    .placeholder(loadAvatarImage(holder, false))
                    .error(loadAvatarImage(holder, false))
                    .into(holder.getChatRoomPic());

        } else if (item.getGroupName() != null) {
            holder.getOpponentName().setText(item.getGroupName());
            Glide.with(mContext).load(item.getImage())
                    .placeholder(loadAvatarImage(holder, true))
                    .dontAnimate()
                    .error(loadAvatarImage(holder, true)).
                    into(holder.getChatRoomPic());
        } else {
            holder.getOpponentName().setText("");
            Glide.with(context).load(loadAvatarImage(holder, false))
                    .placeholder(loadAvatarImage(holder, false))
                    .error(loadAvatarImage(holder, false))
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

    private Drawable loadAvatarImage(ChatRoomViewHolder holder, boolean isgroup) {
        Drawable tempImage;
        if (isgroup) {
            tempImage = mContext.getResources().getDrawable(R.drawable.chat_group);
        } else {
            tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
        }
        if (!Settings.isTitlePicEnabled) {
            return tempImage;
        }
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getRandomColor());
        }
        return tempImage;
    }
}
