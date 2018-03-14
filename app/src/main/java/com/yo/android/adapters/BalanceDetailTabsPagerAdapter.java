package com.yo.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yo.android.ui.fragments.RechargeDetailsFragment;
import com.yo.android.ui.fragments.SpendDetailsFragment;

/**
 * Created by rdoddapaneni on 6/20/2017.
 */

public class BalanceDetailTabsPagerAdapter extends FragmentPagerAdapter {

    private String[] mTitles;

    public BalanceDetailTabsPagerAdapter(FragmentManager fm, String[] titles) {
        super(fm);
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                //return CreditAccountFragment.newInstance();
                return null;
            case 1:
                return RechargeDetailsFragment.newInstance();
            case 2:
                return SpendDetailsFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

}
