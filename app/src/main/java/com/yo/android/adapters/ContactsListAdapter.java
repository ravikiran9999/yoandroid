package com.yo.android.adapters;

import android.content.Context;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.RegisteredContactsViewHolder;
import com.yo.android.model.Registration;

/**
 * Created by rdoddapaneni on 6/29/2016.
 */

public class ContactsListAdapter extends AbstractBaseAdapter<Registration, RegisteredContactsViewHolder> {

    public ContactsListAdapter(Context context) {
        super(context);
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
    public void bindView(int position, RegisteredContactsViewHolder holder, Registration item) {
        holder.getContactNumber().setText(item.getPhoneNumber());
        //holder.getContactMail().setText(item.getEmailId());
    }
}
