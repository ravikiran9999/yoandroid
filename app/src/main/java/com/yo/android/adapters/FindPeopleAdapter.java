package com.yo.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.helpers.FindPeopleViewHolder;
import com.yo.android.model.FindPeople;
import com.yo.android.model.dialer.CallRateDetail;

/**
 * Created by MYPC on 7/17/2016.
 */
public class FindPeopleAdapter extends AbstractBaseAdapter<FindPeople, FindPeopleViewHolder> {

    private Context context;

    public FindPeopleAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public int getLayoutId() {
        return R.layout.find_people_list_item;
    }

    @Override
    public FindPeopleViewHolder getViewHolder(View convertView) {
        return new FindPeopleViewHolder(convertView);
    }

    @Override
    public void bindView(final int position, FindPeopleViewHolder holder, FindPeople item) {

        if (item.getAvatar() == null || TextUtils.isEmpty(item.getAvatar())) {
            Picasso.with(context)
                    .load(R.drawable.ic_contacts)
                    .into(holder.getImvFindPeoplePic());
        } else {
            Picasso.with(context)
                    .load(item.getAvatar())
                    .into(holder.getImvFindPeoplePic());
        }
        holder.getTvFindPeopleName().setText(item.getFirst_name() + " " + item.getLast_name());
        holder.getTvFindPeopleDesc().setText(item.getDescription());
        holder.getBtnFindPeopleFollow().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }

    @Override
    protected boolean hasData(FindPeople findPeople, String key) {
        if(findPeople.getFirst_name()!= null && findPeople.getLast_name()!= null) {
            if (containsValue(findPeople.getFirst_name().toLowerCase(), key)
                    || containsValue(findPeople.getLast_name().toLowerCase(), key)) {
                return true;
            }
        }
        return super.hasData(findPeople, key);
    }

    private boolean containsValue(String str, String key) {
        return str != null && str.toLowerCase().contains(key);
    }
}
