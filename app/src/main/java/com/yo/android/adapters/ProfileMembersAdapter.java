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
        if (item.getAdmin() != null && Boolean.valueOf(item.getAdmin())) {
            holder.getPermission().setText(R.string.admin);
            holder.getPermission().setVisibility(View.VISIBLE);
            holder.getPermission().setTextColor(mContext.getResources().getColor(R.color.black));
        } else {
            holder.getPermission().setVisibility(View.GONE);
        }
    }
}
