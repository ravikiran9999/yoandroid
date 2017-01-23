package com.yo.android.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.CountryCodeActivity;
import com.yo.android.di.Injector;
import com.yo.android.helpers.CountryCodeListViewHolder;
import com.yo.android.helpers.FindPeopleViewHolder;
import com.yo.android.model.CountryCode;
import com.yo.android.model.FindPeople;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.util.Constants;
import com.yo.android.util.CountryCodeHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by mtuity-desk-13 on 23/1/17.
 */

public class CountryCodeListAdapter extends AbstractBaseAdapter<CountryCode, CountryCodeListViewHolder> {
    private Context context;
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private boolean isFollowingUser;

    @Inject
    CountryCodeHelper mCountryCodeHelper;

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;

    private List<CountryCode> list;

    public CountryCodeListAdapter(Context context) {
        super(context);
        this.context = context;
        Injector.obtain(context.getApplicationContext()).inject(this);
        getAllItems();
        mDrawableBuilder = TextDrawable.builder()
                .round();

    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_custom_list;
    }

    @Override
    public boolean isEnabled(int position) {
        if ("true".equals(getItem(position).getCountryCode())) {
            return false;
        }
        return super.isEnabled(position);
    }
    @Override
    public CountryCodeListViewHolder getViewHolder(View convertView) {

        return new CountryCodeListViewHolder(convertView);
    }

    @Override
    public void bindView(final int position, final CountryCodeListViewHolder holder, final CountryCode item) {

        holder.getCountryNameView().setText(item.getCountryName());
        holder.getCountrycodeView().setText("+"+item.getCountryCode());

    }
    @Override
    protected boolean hasData(CountryCode countryCode, String key) {
        if (countryCode.getCountryName() != null && countryCode.getCountryCode() != null) {
            if (containsValue(countryCode.getCountryName().toLowerCase(), key)
                    || containsValue(countryCode.getCountryCode().toLowerCase(), key)) {
                return true;
            }
        }
        return super.hasData(countryCode, key);
    }

    private boolean containsValue(String str, String key) {
        return str != null && str.toLowerCase().contains(key);
    }

}