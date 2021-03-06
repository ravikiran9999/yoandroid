package com.yo.android.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.di.Injector;
import com.yo.android.helpers.ChatRoomViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Room;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by rdoddapaneni on 7/5/2016.
 */

public class ChatRoomListAdapter extends AbstractBaseAdapter<Room, ChatRoomViewHolder> {

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    @Inject
    ContactsSyncManager mContactsSyncManager;

    Context context;
    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    public ChatRoomListAdapter(Context context) {
        super(context);
        Injector.obtain(context.getApplicationContext()).inject(this);
        this.context = context;
        mDrawableBuilder = TextDrawable.builder().round();
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

        if (item.getGroupName() == null) {
            if (TextUtils.isEmpty(item.getFullName()) || TextUtils.isDigitsOnly(item.getFullName())) {
                holder.getOpponentName().setText(Util.numberFromNexgeFormat(item.getMobileNumber()));
            } else {
                holder.getOpponentName().setText(item.getFullName());
            }
            if (!TextUtils.isEmpty(item.getImage())) {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(loadAvatarImage(item, holder, false))
                        .error(loadAvatarImage(item, holder, false))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate();
                Glide.with(mContext).clear(holder.getChatRoomPic());
                Glide.with(mContext).load(item.getImage())
                        .apply(requestOptions)
                        .into(holder.getChatRoomPic());
            } else if (item.getFullName() != null && item.getFullName().length() >= 1 && !TextUtils.isDigitsOnly(item.getFullName())) {
                Glide.with(mContext).clear(holder.getChatRoomPic());
                if (Settings.isTitlePicEnabled) {
                    if (item.getFullName() != null && item.getFullName().length() >= 1) {
                        Drawable drawable = Util.showFirstLetter(mContext, item.getFullName());
                        holder.getChatRoomPic().setImageDrawable(drawable);
                    }
                } else {
                    holder.getChatRoomPic().setImageDrawable(mContext.getResources().getDrawable(R.drawable.dynamic_profile));
                }
            } else {
                holder.getChatRoomPic().setImageDrawable(mContext.getResources().getDrawable(R.drawable.dynamic_profile));

            }
        } else if (item.getGroupName() != null) {
            holder.getOpponentName().setText(item.getGroupName());

            String localFileName = new File(item.getImage()).getName();
            String profilePicImageUri;
            if (localFileName != null && !localFileName.isEmpty()) {
                File file = new File(Environment.getExternalStorageDirectory() + "/YO/" + Constants.YO_PROFILE_PIC + "/" + localFileName);
                profilePicImageUri = file.getAbsolutePath();
            } else {
                profilePicImageUri = item.getImage();
            }

            try {
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(loadAvatarImage(item, holder, true))
                        .priority(Priority.HIGH)
                        .dontAnimate()
                        .error(loadAvatarImage(item, holder, true))
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(mContext).load(item.getImage())
                        .apply(requestOptions)
                        .into(holder.getChatRoomPic());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else

        {
            holder.getOpponentName().setText("");
            RequestOptions requestOptions = new RequestOptions()
                    .dontAnimate()
                    .placeholder(loadAvatarImage(item, holder, false))
                    .error(loadAvatarImage(item, holder, false))
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(context).load(loadAvatarImage(item, holder, false))
                    .apply(requestOptions)
                    .into(holder.getChatRoomPic());
        }

        if (item.isImages())

        {
            holder.getChat().setText(mContext.getResources().getString(R.string.photo));
            holder.getChat().setTextColor(mContext.getResources().getColor(R.color.dialpad_icon_tint));
        } else if (!TextUtils.isEmpty(item.getLastChat()))

        {
            holder.getChat().setText(item.getLastChat());
            holder.getChat().setVisibility(View.VISIBLE);
            holder.getChat().setTextColor(mContext.getResources().getColor(R.color.dialpad_digits_text_color));
        } else

        {
            holder.getChat().setVisibility(View.GONE);
            holder.getChat().setTextColor(mContext.getResources().getColor(R.color.dialpad_digits_text_color));
        }
        holder.getTimeStamp().setText(item.getTimeStamp());
    }

    private Drawable loadAvatarImage(Room item, ChatRoomViewHolder holder, boolean isgroup) {
        Drawable tempImage = null;
        if (isgroup == true) {
            tempImage = mContext.getResources().getDrawable(R.drawable.chat_group);
        } else if (isgroup == false) {
            tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
        } else {
            tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
        }
        if (!Settings.isTitlePicEnabled) {
            return tempImage;
        }
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getColor(item.getFirebaseRoomId()));
        }
        holder.getChatRoomPic().setTag(Settings.imageTag, tempImage);
        return tempImage;
    }
}
