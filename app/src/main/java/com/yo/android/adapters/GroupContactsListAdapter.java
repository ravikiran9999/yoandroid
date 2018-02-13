package com.yo.android.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.helpers.AppRegisteredContactsViewHolder;
import com.yo.android.helpers.GroupContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by rdoddapaneni on 7/18/2016.
 */

public class GroupContactsListAdapter extends AbstractBaseAdapter<Contact, GroupContactsViewHolder> {

    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator;

    public GroupContactsListAdapter(Context context) {
        super(context);
        mDrawableBuilder = TextDrawable.builder().round();
        mColorGenerator = ColorGenerator.MATERIAL;
    }

    @Override
    public int getLayoutId() {
        return R.layout.invite_room_contact;
    }

    @Override
    public GroupContactsViewHolder getViewHolder(View convertView) {
        return new GroupContactsViewHolder(convertView);
    }

    @Override
    public void bindView(int position, GroupContactsViewHolder holder, Contact item) {

        String numberWithCountryCode;
        if (!TextUtils.isEmpty(item.getName()) && (!item.getName().replaceAll("\\s+", "").equalsIgnoreCase(item.getPhoneNo()))) {
            holder.getContactName().setText(item.getName());
            holder.getContactName().setVisibility(View.VISIBLE);
        } else {
            holder.getContactName().setVisibility(View.GONE);
        }

        try {
            if (!TextUtils.isEmpty(item.getImage())) {
                Glide.clear(holder.getContactPic());
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

        if (item.getCountryCode() != null) {
            numberWithCountryCode = "+" + item.getCountryCode().concat(item.getPhoneNo());
        } else {
            numberWithCountryCode = item.getPhoneNo();
        }
        holder.getContactNumber().setText(numberWithCountryCode);
        holder.getCheckBox().setChecked(item.isSelected());
        holder.getCheckBox().setTag(item);

        holder.getCheckBox().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Contact contact = (Contact) cb.getTag();
                contact.setSelected(cb.isChecked());
            }
        });

    }

    private void loadAvatarImage(GroupContactsViewHolder holder, final Contact item) {
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
