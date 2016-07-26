package com.yo.android.helpers;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AbstractViewHolder;

/**
 * Created by rdoddapaneni on 7/25/2016.
 */

public class SelectedContactsViewHolder extends AbstractViewHolder {

    private TextView contactNumber;
    private ImageView delete;

    public SelectedContactsViewHolder(View view) {
        super(view);

        contactNumber = (TextView) view.findViewById(R.id.selected_contact);
        delete = (ImageView) view.findViewById(R.id.delete);
    }

    public TextView getContactNumber() {
        return contactNumber;
    }

    public ImageView getButton() {
        return delete;
    }
}
