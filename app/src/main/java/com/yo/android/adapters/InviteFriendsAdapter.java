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
import com.yo.android.helpers.InviteFriendsViewHolder;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by rdoddapaneni on 7/18/2016.
 */

public class InviteFriendsAdapter extends AbstractBaseAdapter<Contact, InviteFriendsViewHolder> {
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;

    public InviteFriendsAdapter(Context context) {
        super(context);
        mDrawableBuilder = TextDrawable.builder()
                .round();
    }

    @Override
    public int getLayoutId() {
        return R.layout.invite_friends_list_item;
    }

    @Override
    public InviteFriendsViewHolder getViewHolder(View convertView) {
        return new InviteFriendsViewHolder(convertView);
    }

    @Override
    public void bindView(int position, InviteFriendsViewHolder holder, Contact item) {
        holder.getContactNumber().setText(item.getPhoneNo());
        if (item.getName() != null) {
            holder.getContactMail().setText(item.getName());
        }
        if (!TextUtils.isEmpty(item.getImage())) {

            RequestOptions requestOptions = new RequestOptions()
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile);
            Glide.with(mContext)
                    .load(item.getImage())
                    .apply(requestOptions)
                    .transition(withCrossFade())
                    .into(holder.getContactPic());
        } else if (Settings.isTitlePicEnabled) {
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
        }
    }

    private void loadAvatarImage(InviteFriendsViewHolder holder, Contact item) {
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
