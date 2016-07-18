package com.yo.android.adapters;

import android.content.Context;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.AppRegisteredContactsViewHolder;
import com.yo.android.helpers.GroupContactsViewHolder;
import com.yo.android.model.Contact;

/**
 * Created by rdoddapaneni on 7/18/2016.
 */

public class GroupContactsListAdapter extends AbstractBaseAdapter<Contact, GroupContactsViewHolder> {

    public GroupContactsListAdapter(Context context) {
        super(context);
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
        holder.getContactNumber().setText(item.getPhoneNo());
    }
}
