package com.yo.android.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.yo.android.R;
import com.yo.android.helpers.AppRegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.util.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

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
        if (position == 0 && item.getNexgieUserName() == null && item.getPhoneNo() == null && item.getFirebaseRoomId() == null) {
            holder.getContactName().setText(item.getName());
            holder.getContactName().setVisibility(View.VISIBLE);
            holder.getContactNumber().setVisibility(View.GONE);
            holder.getContactPic().setImageDrawable(mContext.getResources().getDrawable(R.drawable.chat_group));
            holder.getInviteContact().setVisibility(View.GONE);
        } else {
            if (!TextUtils.isEmpty(item.getName())) {
                holder.getContactName().setText(item.getName());
                holder.getContactName().setVisibility(View.VISIBLE);
            } else {
                holder.getContactName().setVisibility(View.GONE);
            }

            if (!item.getName().replaceAll("\\s+", "").equalsIgnoreCase(item.getPhoneNo())) {
                holder.getContactNumber().setText(item.getPhoneNo());
                holder.getContactNumber().setVisibility(View.VISIBLE);
            } else {
                holder.getContactNumber().setVisibility(View.GONE);
            }

            if (!item.isYoAppUser()) {
                holder.getInviteContact().setImageResource(R.drawable.ic_invitefriends);
                holder.getInviteContact().setVisibility(View.VISIBLE);
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
                    RequestOptions requestOptions = new RequestOptions()
                            .fitCenter()
                            .placeholder(R.drawable.dynamic_profile)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL);
                    Glide.with(mContext)
                            .load(item.getImage())
                            .apply(requestOptions)
                            //.transition(withCrossFade())
                            .into(holder.getContactPic());
                } else {
                    if (item.getName() != null && item.getName().length() >= 1 && !TextUtils.isDigitsOnly(item.getName())) {
                        Glide.with(mContext).clear(holder.getContactPic());
                        if (Settings.isTitlePicEnabled) {
                            if (item.getName() != null && item.getName().length() >= 1) {
                                String title = String.valueOf(item.getName().charAt(0)).toUpperCase();
                                Pattern p = Pattern.compile("^[a-zA-Z]");
                                Matcher m = p.matcher(title);
                                boolean b = m.matches();
                                if (b) {
                                    Drawable drawable = mDrawableBuilder.build(title, mColorGenerator.getColor(item.getPhoneNo()));
                                    holder.getContactPic().setImageDrawable(drawable);
                                } else {
                                    loadAvatarImage(holder, item);
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

    private void loadAvatarImage(AppRegisteredContactsViewHolder holder, final Contact item) {
        Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getColor(item.getPhoneNo()));
        }
        if (holder.getContactPic().getTag(Settings.imageTag) == null) {
            holder.getContactPic().setTag(Settings.imageTag, tempImage);
        }
        holder.getContactPic().setImageDrawable((Drawable) holder.getContactPic().getTag(Settings.imageTag));
    }
}
