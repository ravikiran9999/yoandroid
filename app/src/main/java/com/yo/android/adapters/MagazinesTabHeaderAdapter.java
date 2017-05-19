package com.yo.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yo.android.ui.fragments.findpeople.FindPeopleFragment;
import com.yo.android.ui.fragments.findpeople.FollowersFragment;
import com.yo.android.ui.fragments.findpeople.FollowingsFragment;

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
                return FindPeopleFragment.newInstance();
            case 1:
                return FollowingsFragment.newInstance();
            case 2:
                return FollowersFragment.newInstance();
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
