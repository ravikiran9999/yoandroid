package com.yo.android.adapters;

import android.content.Context;
import android.view.View;
import com.yo.android.R;
import com.yo.android.helpers.InviteFriendsViewHolder;
import com.yo.android.model.Contact;

/**
 * Created by rdoddapaneni on 7/18/2016.
 */

public class InviteFriendsAdapter extends AbstractBaseAdapter<Contact, InviteFriendsViewHolder> {

    public InviteFriendsAdapter(Context context) {
        super(context);
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
