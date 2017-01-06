package com.yo.android.adapters;

import android.content.Context;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.InviteFriendsViewHolder;
import com.yo.android.model.Contact;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;

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

    }
}
