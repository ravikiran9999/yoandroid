package com.yo.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rdoddapaneni on 6/24/2016.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    private ProgressBar progressBar;

    public TabsPagerAdapter(final FragmentManager manager, ProgressBar progressBar) {
        super(manager);
        this.progressBar = progressBar;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    /**
     * @param fragment
     * @param title
     */
    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (position == 3) {
            //progressBar.showProgress();
        }

        super.setPrimaryItem(container, position, object);
    }

    public interface ProgressBar {
        void showProgress();
    }
}
