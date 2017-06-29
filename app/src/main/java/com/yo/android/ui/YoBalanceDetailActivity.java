package com.yo.android.ui;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;

import com.yo.android.R;
import com.yo.android.adapters.BalanceDetailTabsPagerAdapter;
import com.yo.android.widgets.ScrollTabHolder;

import butterknife.Bind;
import butterknife.ButterKnife;


public class YoBalanceDetailActivity extends BaseActivity implements
        ScrollTabHolder, ViewPager.OnPageChangeListener {

    @Bind(R.id.header)
    LinearLayout headerLayout;
    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.tablayout)
    TabLayout tabLayout;

    public int mMinHeaderTranslation;
    public int mHeaderHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yo_balance_detail);
        ButterKnife.bind(this);

        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_full_height);
        mMinHeaderTranslation = -mHeaderHeight + getResources().getDimensionPixelSize(R.dimen.tablayout_height);
    }

    private void setupTabPager() {
        String[] titles = getResources().getStringArray(R.array.balance_details_tabs_titles);
        BalanceDetailTabsPagerAdapter viewPagerAdapter =
                new BalanceDetailTabsPagerAdapter(getSupportFragmentManager(), titles);
        viewPager.addOnPageChangeListener(this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        // "android:switcher:" is a famous workaround to get a Fragment from ViewPager by id
        Fragment currentFragment =
                getSupportFragmentManager().findFragmentByTag(
                        "android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());

        if (currentFragment != null && currentFragment instanceof ScrollTabHolder) {
            ((ScrollTabHolder) currentFragment)
                    .adjustScroll((int) (headerLayout.getHeight() + headerLayout.getTranslationY()),
                            mHeaderHeight, mMinHeaderTranslation);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void adjustScroll(int scrollHeight, int headerTranslationY, int minHeaderTranslation) {

    }

    @Override
    public void onScroll(int currentScrollY) {
        headerLayout.setTranslationY(Math.max(-currentScrollY, mMinHeaderTranslation));
    }
}
