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
import com.yo.android.helpers.ProfileMembersViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.GroupMembers;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by rdoddapaneni on 7/26/2016.
 */

public class ProfileMembersAdapter extends AbstractBaseAdapter<GroupMembers, ProfileMembersViewHolder> {

    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator;

    public ProfileMembersAdapter(Context context) {
        super(context);
        mDrawableBuilder = TextDrawable.builder().round();
        mColorGenerator = ColorGenerator.MATERIAL;
    }

    @Override
    public int getLayoutId() {
        return R.layout.profile_members_list_item;
    }

    @Override
    public ProfileMembersViewHolder getViewHolder(View convertView) {
        return new ProfileMembersViewHolder(convertView);
    }

    @Override
    public void bindView(int position, ProfileMembersViewHolder holder, GroupMembers item) {
        String fullName = item.getUserProfile().getFullName();
        String mobileNumber = item.getUserProfile().getPhoneNumber();
        String countryCode = "+" + item.getUserProfile().getCountryCode();
        String fullPhoneNumber = countryCode.concat(mobileNumber);

        if (item.getUserProfile() != null && fullName != null && !fullName.replaceAll("\\s+", "").equalsIgnoreCase(mobileNumber)) {
            holder.getName().setText(fullName);
            holder.getName().setVisibility(View.VISIBLE);
        } else {
            holder.getName().setVisibility(View.GONE);
        }

        if (item.getUserProfile() != null && fullName != null && mobileNumber != null && !fullName.equalsIgnoreCase(mContext.getString(R.string.you)) && !fullName.replaceAll("\\s+", "").equalsIgnoreCase(fullPhoneNumber)) {
            holder.getContactNumber().setText(fullPhoneNumber);
            holder.getContactNumber().setVisibility(View.VISIBLE);
        } else {
            holder.getContactNumber().setVisibility(View.GONE);
        }

        try {
            Glide.with(mContext).clear(holder.getImageView());
            if (!TextUtils.isEmpty(item.getUserProfile().getImage())) {
                RequestOptions requestOptions = new RequestOptions()
                        .fitCenter()
                        .placeholder(R.drawable.dynamic_profile)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(mContext)
                        .load(item.getUserProfile().getImage())
                        .apply(requestOptions)
                        .transition(withCrossFade())
                        .into(holder.getImageView());
            } else if (fullName != null && fullName.length() >= 1 && !TextUtils.isDigitsOnly(fullName)) {

                if (Settings.isTitlePicEnabled) {
                    if (fullName.length() >= 1) {
                        String title = String.valueOf(fullName.charAt(0)).toUpperCase();
                        Pattern p = Pattern.compile("^[a-zA-Z]");
                        Matcher m = p.matcher(title);
                        boolean b = m.matches();
                        if (b) {
                            Drawable drawable = mDrawableBuilder.build(title, mColorGenerator.getColor(fullPhoneNumber));
                            holder.getImageView().setImageDrawable(drawable);
                        } else {
                            loadAvatarImage(holder, fullPhoneNumber);
                        }
                    }else{
                        holder.getImageView().setImageDrawable(null);//To aviod loading previous image
                    }
                } else {
                    holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.dynamic_profile));
                }
            } else {
                holder.getImageView().setImageDrawable(null);//To aviod loading previous image
                holder.getImageView().setImageDrawable(mContext.getResources().getDrawable(R.drawable.dynamic_profile));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setAdmin(holder, item.getAdmin());
    }

    private void loadAvatarImage(ProfileMembersViewHolder holder, final String phoneNumber) {
        Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getColor(phoneNumber));
        }
        if (holder.getImageView().getTag(Settings.imageTag) == null) {
            holder.getImageView().setTag(Settings.imageTag, tempImage);
        }
        holder.getImageView().setImageDrawable((Drawable) holder.getImageView().getTag(Settings.imageTag));
    }

    private void setAdmin(ProfileMembersViewHolder holder, String admin) {
        if (admin != null && Boolean.valueOf(admin)) {
            holder.getPermission().setText(R.string.admin);
            holder.getPermission().setVisibility(View.VISIBLE);
            holder.getPermission().setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            holder.getPermission().setVisibility(View.GONE);
        }
    }
}
