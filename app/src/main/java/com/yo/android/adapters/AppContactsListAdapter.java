package com.yo.android.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.helpers.AppRegisteredContactsViewHolder;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.util.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rdoddapaneni on 7/5/2016.
 */

public class AppContactsListAdapter extends AbstractBaseAdapter<Contact, AppRegisteredContactsViewHolder> {

    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator;

    public AppContactsListAdapter(Context context) {
        super(context);
        mDrawableBuilder = TextDrawable.builder().round();
        mColorGenerator = ColorGenerator.MATERIAL;
    }

    public void addItem(Contact contact) {
        getAllItems().add(contact);
        notifyDataSetChanged();
    }

    @Override
    public int getLayoutId() {
        return R.layout.app_contacts_list_item;
    }

    @Override
    public AppRegisteredContactsViewHolder getViewHolder(View convertView) {
        return new AppRegisteredContactsViewHolder(convertView);
    }

    @Override
    public void bindView(int position, AppRegisteredContactsViewHolder holder, final Contact item) {
        if (position == 0 && item.getVoxUserName() == null && item.getPhoneNo() == null && item.getFirebaseRoomId() == null) {
            holder.getContactName().setVisibility(View.VISIBLE);
            holder.getContactName().setText(item.getName());
            holder.getContactNumber().setVisibility(View.GONE);
            holder.getContactPic().setImageDrawable(mContext.getResources().getDrawable(R.drawable.chat_group));
            holder.getInviteContact().setVisibility(View.GONE);
        } else {
            if (TextUtils.isEmpty(item.getName())) {
                holder.getContactName().setVisibility(View.GONE);
            } else {
                holder.getContactName().setVisibility(View.VISIBLE);
                holder.getContactName().setText(item.getName());
            }
            if (!item.getName().replaceAll("\\s+", "").equalsIgnoreCase(item.getPhoneNo())) {
                holder.getContactNumber().setText(item.getPhoneNo());

            } else {
                holder.getContactNumber().setVisibility(View.GONE);
            }

            if (!item.getYoAppUser()) {
                holder.getInviteContact().setVisibility(View.VISIBLE);
                holder.getInviteContact().setImageResource(R.drawable.ic_invitefriends);
            } else {
                holder.getInviteContact().setVisibility(View.GONE);
            }

            holder.getInviteContact().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.inviteFriend(mContext, item.getPhoneNo());
                }
            });

            try {
                if (!TextUtils.isEmpty(item.getImage())) {

                    Glide.with(mContext)
                            .load(item.getImage())
                            .fitCenter()
                            .placeholder(R.drawable.dynamic_profile)
                            .crossFade()
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.getContactPic());
                } else {
                    if (item.getName() != null && item.getName().length() >= 1 && !TextUtils.isDigitsOnly(item.getName())) {
                        if (Settings.isTitlePicEnabled) {
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
                            holder.getContactPic().setImageDrawable(mContext.getResources().getDrawable(R.drawable.dynamic_profile));
                        }
                    } else {
                        holder.getContactPic().setImageDrawable(mContext.getResources().getDrawable(R.drawable.dynamic_profile));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void loadAvatarImage(AppRegisteredContactsViewHolder holder) {
        Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getRandomColor());
        }
        if (holder.getContactPic().getTag(Settings.imageTag) == null) {
            holder.getContactPic().setTag(Settings.imageTag, tempImage);
        }
        holder.getContactPic().setImageDrawable((Drawable) holder.getContactPic().getTag(Settings.imageTag));
    }
}
