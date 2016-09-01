package com.yo.android.adapters;

import android.content.Context;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.ProfileMembersViewHolder;
import com.yo.android.model.GroupMembers;
import com.yo.android.model.Room;

/**
 * Created by rdoddapaneni on 7/26/2016.
 */

public class ProfileMembersAdapter extends AbstractBaseAdapter<GroupMembers, ProfileMembersViewHolder> {

    public ProfileMembersAdapter(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.app_contacts_list_item;
    }

    @Override
    public ProfileMembersViewHolder getViewHolder(View convertView) {
        return new ProfileMembersViewHolder(convertView);
    }

    @Override
    public void bindView(int position, ProfileMembersViewHolder holder, GroupMembers item) {
        //if(item.getGroupName() != null) {
            holder.getContactNumber().setText(item.getUserProfile().getFullName());
        if(item.getAdmin().equalsIgnoreCase("true")) {
            holder.getPermission().setText("admin");
        } else if(item.getAdmin().equalsIgnoreCase("false")) {
            holder.getPermission().setText("");
        }

        //holder.getContactMail().setText(item.getEmailId());
        //}
    }
}
