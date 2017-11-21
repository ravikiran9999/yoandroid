package com.yo.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactsListAdapter extends AbstractBaseAdapter<Contact, RegisteredContactsViewHolder> {

    private Context context;
    private String userId;
    private TextDrawable.IBuilder mDrawableBuilder;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;

    public ContactsListAdapter(Context context, String userId) {
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

        if (!TextUtils.isEmpty(item.getName())) {
            String numberWithCountryCode;
            if (TextUtils.isDigitsOnly(item.getName().replaceAll("\\s+", ""))) {
                if (item.getCountryCode() != null) {
                    numberWithCountryCode = "+" + item.getCountryCode().concat(item.getPhoneNo());
                } else {
                    numberWithCountryCode = item.getPhoneNo();
                }
                holder.getContactNumber().setText(numberWithCountryCode);
            } else {
                holder.getContactNumber().setText(item.getName());
            }
            holder.getContactNumber().setVisibility(View.VISIBLE);
        } else {
            holder.getContactNumber().setVisibility(View.GONE);
        }

        if (item.getPhoneNo() != null) {
            item.setPhoneNo(item.getPhoneNo().trim());
        }

        if ((item.getName() != null) && (!item.getName().replaceAll("\\s+", "").equalsIgnoreCase(item.getPhoneNo()))) {
            String numberWithCountryCode;
            if (item.getCountryCode() != null) {
                numberWithCountryCode = "+" + item.getCountryCode().concat(item.getPhoneNo());
            } else {
                numberWithCountryCode = item.getPhoneNo();
            }
            holder.getContactMail().setText(numberWithCountryCode);
            holder.getContactMail().setVisibility(View.VISIBLE);

        } else {
            holder.getContactMail().setVisibility(View.GONE);
        }



        if (!TextUtils.isEmpty(item.getImage())) {

            Glide.with(mContext)
                    .load(item.getImage())
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .crossFade()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile)
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
        } else {
            loadAvatarImage(holder, item);
        }

        holder.getMessageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.isYoAppUser()) {

                    navigateToChatScreen(item);

                } else {
                    Util.inviteFriend(context, item.getPhoneNo());
                }
            }
        });

        holder.getCallView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Registration registration = getAllItems().get(position);
                //String opponentPhoneNumber = registration.getPhoneNumber();
                String opponentPhoneNumber = item.getNexgieUserName();

                if (opponentPhoneNumber != null) {
                    SipHelper.makeCall(mContext, opponentPhoneNumber,false);
                } else {
                    if (item.getCountryCode() != null && item.getPhoneNo() != null) {
                        SipHelper.makeCall(mContext, item.getCountryCode() + item.getPhoneNo(),true);
                    } else {
                        //TODO: Think about it
                    }
                }
            }
        });

        if (item.isYoAppUser()) {
            holder.getMessageView().setImageResource(R.drawable.ic_new_chat);
            holder.getCallView().setImageResource(R.drawable.yo_call_free);
        } else {
            holder.getMessageView().setImageResource(R.drawable.ic_invitefriends);
            holder.getCallView().setImageResource(R.drawable.ic_new_call);
        }

    }

    private void loadAvatarImage(RegisteredContactsViewHolder holder, Contact item) {
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

    private void navigateToChatScreen(Contact contact) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.CONTACT, contact);
        intent.putExtra(Constants.TYPE, Constants.CONTACT);
        context.startActivity(intent);

    }
}
