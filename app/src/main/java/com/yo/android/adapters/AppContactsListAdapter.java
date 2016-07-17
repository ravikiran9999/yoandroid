package com.yo.android.adapters;

import android.content.Context;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.AppRegisteredContactsViewHolder;
import com.yo.android.model.Registration;
import com.yo.android.model.YoAppContacts;

/**
 * Created by rdoddapaneni on 7/5/2016.
 */

public class AppContactsListAdapter extends AbstractBaseAdapter<YoAppContacts, AppRegisteredContactsViewHolder> {

    public AppContactsListAdapter(Context context) {
        super(context);
    }


    @Override
    public int getLayoutId() {
        return R.layout.app_contacts_list_item;
    }

    @Override
    public AppRegisteredContactsViewHolder getViewHolder(View convertView) {
        return new AppRegisteredContactsViewHolder(convertView);
    }

    @Override
    public void bindView(int position, AppRegisteredContactsViewHolder holder, YoAppContacts item) {
        holder.getContactNumber().setText(item.getId());
        //holder.getContactMail().setText(item.getEmailId());
    }
}
