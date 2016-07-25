package com.yo.android.adapters;

import android.content.Context;
import android.view.View;

import com.yo.android.R;
import com.yo.android.helpers.SelectedContactsViewHolder;
import com.yo.android.model.Contact;

import java.util.ArrayList;

/**
 * Created by rdoddapaneni on 7/25/2016.
 */

public class SelectedContactsAdapter extends AbstractBaseAdapter<Contact, SelectedContactsViewHolder> {

    private ArrayList<Contact> contactArrayList;


    public SelectedContactsAdapter(Context context) {
        super(context);
    }

    public SelectedContactsAdapter(Context context, ArrayList<Contact> contactArrayList) {
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
        holder.getContactNumber().setText(item.getPhoneNo());
        holder.getButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactArrayList.remove(item);
                removeItem(item);
            }
        });
    }
}
