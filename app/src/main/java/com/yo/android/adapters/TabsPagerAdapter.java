package com.yo.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yo.android.ui.fragments.CallFragment;
import com.yo.android.ui.fragments.ChatFragment;
import com.yo.android.ui.fragments.ContactsFragment;
import com.yo.android.ui.fragments.MagazinesFragment;

/**
 * Created by rdoddapaneni on 6/24/2016.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter {

    private String[] tabs = {"magazines", "calls", "chats", "contacts"};

    /**
     *
     * @param fm fragment manager
     */
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return new MagazinesFragment();
        }
        if(position == 1) {
            return new CallFragment();
        }
        if(position == 2) {
            return new ChatFragment();
        }
        if(position == 3) {
            return new ContactsFragment();
        }
        return new ChatFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position].toUpperCase();
    }

    @Override
    public int getCount() {
        return tabs.length;
    }
}
