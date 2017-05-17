package com.yo.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by rdoddapaneni on 5/17/2017.
 */

public class MagazinesTabHeaderAdapter extends FragmentPagerAdapter {

    private String[] mTitles;

    public MagazinesTabHeaderAdapter(FragmentManager fm, String[] mTitles) {
        super(fm);
        this.mTitles = mTitles;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                //return MyBookingFragment.newInstance(mMyTrip);
            case 1:
                //return TripInfoFragment.newInstance(mMyTrip);
            case 2:
                //return StationInfoFragment.newInstance(mMyTrip);
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
