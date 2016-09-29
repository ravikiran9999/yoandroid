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
        if (item.getUserProfile() != null && item.getUserProfile().getFullName() != null) {
            holder.getName().setText(item.getUserProfile().getFullName());
        } else {
            holder.getName().setVisibility(View.GONE);
        }
        if (item.getUserProfile() != null && item.getUserProfile().getMobileNumber() != null) {
            holder.getContactNumber().setText(item.getUserProfile().getMobileNumber());
        } else {
            holder.getContactNumber().setVisibility(View.GONE);
        }
        if (item.getAdmin() != null && Boolean.valueOf(item.getAdmin())) {
            holder.getPermission().setText(R.string.admin);
            holder.getPermission().setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            holder.getPermission().setVisibility(View.GONE);
        }
    }
}
