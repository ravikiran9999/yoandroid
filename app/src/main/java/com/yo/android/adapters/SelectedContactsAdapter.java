package com.yo.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.yo.android.R;
import com.yo.android.chat.ui.CreateGroupActivity;
import com.yo.android.helpers.SelectedContactsViewHolder;
import com.yo.android.model.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rdoddapaneni on 7/25/2016.
 */

public class SelectedContactsAdapter extends AbstractBaseAdapter<Contact, SelectedContactsViewHolder> {

    private List<Contact> contactArrayList;

    public SelectedContactsAdapter(Context context) {
        super(context);
    }

    public SelectedContactsAdapter(Context context, List<Contact> contactArrayList) {
        super(context);
        this.contactArrayList = contactArrayList;
    }

    @Override
    public int getLayoutId() {
        return R.layout.selected_contacts_item;
    }

    @Override
    public SelectedContactsViewHolder getViewHolder(View convertView) {
        return new SelectedContactsViewHolder(convertView);
    }

    @Override
    public void bindView(final int position, SelectedContactsViewHolder holder, final Contact item) {
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

        holder.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactArrayList.remove(item);
                removeItem(item);
                for (int i = 0; i < CreateGroupActivity.ContactsArrayList.size(); i++) {
                    if (CreateGroupActivity.ContactsArrayList.get(i).getPhoneNo().equalsIgnoreCase(item.getPhoneNo())) {
                        CreateGroupActivity.ContactsArrayList.get(i).setSelected(false);
                    }
                }
            }
        });
    }
}
