package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.TabsPagerAdapter;
import com.yo.android.chat.ui.ChatFragment;
import com.yo.android.chat.ui.ContactsFragment;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.voip.SipService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ramesh on 3/7/16.
 */
public class BottomTabsActivity extends BaseActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private List<TabsData> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_tabs);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), null);
        mAdapter.addFragment(new MagazinesFragment(), null);
        mAdapter.addFragment(new DialerFragment(), null);
        mAdapter.addFragment(new ChatFragment(), null);
        mAdapter.addFragment(new ContactsFragment(), null);
        mAdapter.addFragment(new ContactsFragment(), null);
        viewPager.setAdapter(mAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
//        initTabs();
        dataList = getData();
        for (int i = 0; i < dataList.size(); i++) {
            final TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(setTabs(dataList.get(i).getTitle(), dataList.get(i).getDrawableId()));
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                try {
                    setToolBarTitle((dataList.get(position)).getTitle());
                } catch (Exception e) {
                    mLog.w("onPageSelected", e);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //
        Intent in = new Intent(getApplicationContext(), SipService.class);
        startService(in);


    }


    public void setToolBarTitle(String title) {
        final TextView titleView = (TextView) toolbar.findViewById(R.id.title);
        titleView.setText(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getSupportFragmentManager().findFragmentById(R.id.content) != null) {
            getSupportFragmentManager().findFragmentById(R.id.content).onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initTabs() {
        //
        //
        final TabLayout.Tab magazinesTab = tabLayout.getTabAt(0);
        magazinesTab.setCustomView(setTabs("Magazines", R.drawable.ic_magazine));
        //
        final TabLayout.Tab dialerTab = tabLayout.getTabAt(1);
        dialerTab.setCustomView(setTabs("Dialer", R.drawable.ic_dialer));
        //
        final TabLayout.Tab chatTab = tabLayout.getTabAt(2);
        chatTab.setCustomView(setTabs("Chats", R.drawable.ic_chat));

        final TabLayout.Tab contactsTab = tabLayout.getTabAt(3);
        contactsTab.setCustomView(setTabs("Contacts", R.drawable.ic_contacts));
        //
        final TabLayout.Tab moreTab = tabLayout.getTabAt(4);
        moreTab.setCustomView(setTabs("More", R.drawable.ic_more));
    }

    public View setTabs(final String title, final int resId) {
        final View view = LayoutInflater.from(this).inflate(R.layout.acivity_tab_holder, null);
        // We need to manually set the LayoutParams here because we don't have a view root
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((ImageView) view.findViewById(R.id.image)).setImageResource(resId);
        ((TextView) view.findViewById(R.id.text)).setText(title);
        return view;
    }

    protected List<TabsData> getData() {
        List<TabsData> list = new ArrayList<>();
        list.add(new TabsData("Magazines", R.drawable.ic_magazine));
        list.add(new TabsData("Dialer", R.drawable.ic_dialer));
        list.add(new TabsData("Chats", R.drawable.ic_chat));
        list.add(new TabsData("Contacts", R.drawable.ic_contacts));
        list.add(new TabsData("More", R.drawable.ic_more));
        return list;
    }

    public class TabsData {

        private String title;
        private int drawableId;

        public TabsData(String title, int drawableId) {
            this.title = title;
            this.drawableId = drawableId;
        }

        public String getTitle() {
            return title;
        }

        public int getDrawableId() {
            return drawableId;
        }
    }

}
