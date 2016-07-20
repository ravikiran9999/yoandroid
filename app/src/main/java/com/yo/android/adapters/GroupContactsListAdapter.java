package com.yo.android.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.CheckBox;

import com.yo.android.R;
import com.yo.android.helpers.AppRegisteredContactsViewHolder;
import com.yo.android.helpers.GroupContactsViewHolder;
import com.yo.android.model.Contact;

import java.util.ArrayList;

/**
 * Created by rdoddapaneni on 7/18/2016.
 */

public class GroupContactsListAdapter extends AbstractBaseAdapter<Contact, GroupContactsViewHolder> {

    private ArrayList<Contact> mSelectedItems;

    public GroupContactsListAdapter(Context context) {
        super(context);
        mSelectedItems = new ArrayList<>();
    }

    public ArrayList<Contact> getmSelectedItems() {
        return mSelectedItems;
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
        holder.getCheckBox().setChecked(item.isSelected());
        holder.getCheckBox().setTag(item);

        holder.getCheckBox().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Contact contact = (Contact) cb.getTag();
                contact.setSelected(cb.isChecked());
                mSelectedItems.add(contact);
            }
        });

    }
}
