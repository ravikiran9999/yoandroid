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
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.model.FindPeople;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by creatives on 10/3/2016.
 */
public class TransferBalanceContactAdapter extends AbstractBaseAdapter<Contact, RegisteredContactsViewHolder> {

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
    public void bindView(final int position, RegisteredContactsViewHolder holder, final Contact item) {

        holder.getContactNumber().setText(item.getName());
        holder.getContactNumber().setVisibility(View.VISIBLE);
        /*if (item.getPhoneNo() != null) {
            //item.setPhone_no(item.getPhone_no().trim());
        }*/

        if ((item.getName() != null) && (!item.getName().replaceAll("\\s+", "").equalsIgnoreCase(item.getPhoneNo()))) {
            //holder.getContactMail().setText(item.getPhoneNo());
            holder.getContactMail().setText(phoneNumberWithCountryCodeFormat(item));
            holder.getContactMail().setVisibility(View.VISIBLE);

        } else {
            holder.getContactMail().setVisibility(View.GONE);
        }
        Glide.with(mContext).clear(holder.getContactPic());
        if (!TextUtils.isEmpty(item.getImage())) {
            RequestOptions requestOptions = new RequestOptions()
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile);
            Glide.with(mContext)
                    .load(item.getImage())
                    .apply(requestOptions)
                    .transition(withCrossFade())
                    .into(holder.getContactPic());
        } else if (Settings.isTitlePicEnabled) {
            if (!TextUtils.isEmpty(item.getName()) && item.getName().length() >= 1) {
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
            RequestOptions requestOptions = new RequestOptions()
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile);
            Glide.with(mContext)
                    .load("")
                    .apply(requestOptions)
                    .transition(withCrossFade())
                    .into(holder.getContactPic());
        }
    }

    @Override
    protected boolean hasData(Contact contact, String key) {
        if (contact.getName() != null && contact.getName() != null && contact.getPhoneNo() != null) {
            if (containsValue(contact.getName().toLowerCase(), key)
                    || containsValue(contact.getName().toLowerCase(), key)
                    || containsValue(contact.getPhoneNo().toLowerCase(), key)) {
                return true;
            }
        }
        return super.hasData(contact, key);
    }

    private boolean containsValue(String str, String key) {
        return str != null && str.toLowerCase().contains(key);
    }

    private String phoneNumberWithCountryCodeFormat(Contact mContact) {
        if (mContact != null && !TextUtils.isEmpty(mContact.getCountryCode()) && !TextUtils.isEmpty(mContact.getPhoneNo())) {
            return context.getString(R.string.phone_number_with_code_plus, mContact.getCountryCode(), mContact.getPhoneNo());
        }

        return mContact != null ? mContact.getPhoneNo() : null;
    }
}
