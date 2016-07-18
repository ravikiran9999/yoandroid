package com.yo.android.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.TabsPagerAdapter;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 15/7/16.
 */
public class OthersProfileActivity extends BaseActivity {
    public static TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView backbtn;
    TabsPagerAdapter mAdapter;
    private List<ProfileTabsData> dataList;
    String userId;

    private static Fragment currentFragment;

    public static Fragment getCurrentFragment() {
        return currentFragment;
    }

    public static void setCurrentFragment(Fragment currentFragment) {
        OthersProfileActivity.currentFragment = currentFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        backbtn = (ImageView) findViewById(R.id.back);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new OthersProfileMagazines(), null);
        mAdapter.addFragment(new OtherProfilesFollowers(), null);
        mAdapter.addFragment(new OtherProfilesLinedArticles(), null);
        viewPager.setAdapter(mAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        dataList = createTabsList();
        int index = 0;
        for (ProfileTabsData data : dataList) {
            final TabLayout.Tab tab = tabLayout.getTabAt(index);
            if (index == 2) {
                tab.setCustomView(setTabs(data.getTitle(), data.getCount(), true));
            } else {
                tab.setCustomView(setTabs(data.getTitle(), data.getCount(), false));
            }
            index++;
        }


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        viewPager.setCurrentItem(1);
        viewPager.setCurrentItem(0);

        userId = getIntent().getStringExtra(Constants.USER_ID);

    }

    public View setTabs(final String title, final int count, final boolean isLast) {
        final View view = LayoutInflater.from(this).inflate(R.layout.profile_tab_holder, null);
        // We need to manually set the LayoutParams here because we don't have a view root
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((TextView) view.findViewById(R.id.count)).setText(String.valueOf(count));
        ((TextView) view.findViewById(R.id.tab_name)).setText(title);
        if (isLast) {
            ((View) view.findViewById(R.id.divider)).setVisibility(View.GONE);
        }

        return view;
    }

    protected List<ProfileTabsData> createTabsList() {
        List<ProfileTabsData> list = new ArrayList<>();
        list.add(new ProfileTabsData("Magazines", 0));
        list.add(new ProfileTabsData("Followers", 0));
        list.add(new ProfileTabsData("Liked Articles", 0));

        return list;
    }


    public class ProfileTabsData {

        private String title;
        private int count;

        public ProfileTabsData(String title, int count) {
            this.title = title;
            this.count = count;
        }

        public String getTitle() {
            return title;
        }

        public int getCount() {
            return count;
        }
    }
}
