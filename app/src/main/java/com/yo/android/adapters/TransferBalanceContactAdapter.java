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
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.FindPeople;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by creatives on 10/3/2016.
 */
public class TransferBalanceContactAdapter extends AbstractBaseAdapter<FindPeople, RegisteredContactsViewHolder> {

    private Context context;
    private String userId;
    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    public TransferBalanceContactAdapter(Context context, String userId) {
        super(context);
        this.context = context;
        this.userId = userId;
        mDrawableBuilder = TextDrawable.builder()
                .round();

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
    public void bindView(final int position, RegisteredContactsViewHolder holder, final FindPeople item) {

            holder.getContactNumber().setText(item.getFirst_name() + " " + item.getLast_name());
            holder.getContactNumber().setVisibility(View.VISIBLE);
        if (item.getPhone_no() != null) {
            item.setPhone_no(item.getPhone_no().trim());
        }

        if ((item.getFirst_name() != null) && (!item.getFirst_name().replaceAll("\\s+", "").equalsIgnoreCase(item.getPhone_no()))) {
            holder.getContactMail().setText(item.getPhone_no());
            holder.getContactMail().setVisibility(View.VISIBLE);

        } else {
            holder.getContactMail().setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(item.getAvatar())) {

            Glide.with(mContext)
                    .load(item.getAvatar())
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile)
                    .into(holder.getContactPic());
        } else if (Settings.isTitlePicEnabled) {
            if (item.getFirst_name() != null && item.getFirst_name().length() >= 1) {
                String title = String.valueOf(item.getFirst_name().charAt(0)).toUpperCase();
                Pattern p = Pattern.compile("^[a-zA-Z]");
                Matcher m = p.matcher(title);
                boolean b = m.matches();
                if (b) {
                    Drawable drawable = mDrawableBuilder.build(title, mColorGenerator.getRandomColor());
                    Glide.clear(holder.getContactPic());
                    holder.getContactPic().setImageDrawable(drawable);
                } else {
                    loadAvatarImage(holder);
                }
            }
        } else {
            loadAvatarImage(holder);
        }

        holder.getMessageView().setVisibility(View.GONE);
        holder.getCallView().setVisibility(View.GONE);
    }

    private void loadAvatarImage(RegisteredContactsViewHolder holder) {
        Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            shape.setColor(mColorGenerator.getRandomColor());
            if (holder.getContactPic().getTag(Settings.imageTag) == null) {
                holder.getContactPic().setTag(Settings.imageTag, tempImage);
            }
            holder.getContactPic().setImageDrawable((Drawable) holder.getContactPic().getTag(Settings.imageTag));
        } else {
            Glide.with(mContext)
                    .load("")
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile)
                    .into(holder.getContactPic());
        }
    }

    @Override
    protected boolean hasData(FindPeople findPeople, String key) {
        if (findPeople.getFirst_name() != null && findPeople.getLast_name() != null && findPeople.getPhone_no() != null) {
            if (containsValue(findPeople.getFirst_name().toLowerCase(), key)
                    || containsValue(findPeople.getLast_name().toLowerCase(), key)
                    || containsValue(findPeople.getPhone_no().toLowerCase(), key)) {
                return true;
            }
        }
        return super.hasData(findPeople, key);
    }

    private boolean containsValue(String str, String key) {
        return str != null && str.toLowerCase().contains(key);
    }
}
