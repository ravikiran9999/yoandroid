package com.yo.android.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
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
import com.yo.android.chat.ui.fragments.ChatFragment;
import com.yo.android.chat.ui.fragments.ContactsFragment;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.ui.fragments.MoreFragment;
import com.yo.android.voip.SipService;
import com.yo.android.vox.BalanceHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by Ramesh on 3/7/16.
 */
public class BottomTabsActivity extends BaseActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private List<TabsData> dataList;
    @Inject
    BalanceHelper balanceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_tabs);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new MagazinesFragment(), null);
        mAdapter.addFragment(new DialerFragment(), null);
        mAdapter.addFragment(new ChatFragment(), null);
        mAdapter.addFragment(new ContactsFragment(), null);
        mAdapter.addFragment(new MoreFragment(), null);
        viewPager.setAdapter(mAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        dataList = createTabsList();
        for (int i = 0; i < dataList.size(); i++) {
            final TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(setTabs(dataList.get(i).getTitle(), dataList.get(i).getDrawable()));
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
        balanceHelper.checkBalance();

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

    public View setTabs(final String title, final Drawable drawable) {
        final View view = LayoutInflater.from(this).inflate(R.layout.acivity_tab_holder, null);
        // We need to manually set the LayoutParams here because we don't have a view root
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((ImageView) view.findViewById(R.id.image)).setImageDrawable(drawable);
        ((TextView) view.findViewById(R.id.text)).setText(title);
        return view;
    }

    protected List<TabsData> createTabsList() {
        List<TabsData> list = new ArrayList<>();
        list.add(new TabsData("Magazines", getResources().getDrawable(R.drawable.tab_magazines)));
        list.add(new TabsData("Dialer", getResources().getDrawable(R.drawable.tab_dialer)));
        list.add(new TabsData("Chats", getResources().getDrawable(R.drawable.tab_chats)));
        list.add(new TabsData("Contacts", getResources().getDrawable(R.drawable.tab_contacts)));
        list.add(new TabsData("More", getResources().getDrawable(R.drawable.tab_more)));
        return list;
    }

    public Drawable createStateList(int normal, int selected) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{}, getResources().getDrawable(selected));
        states.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(normal));
        return states;
    }


    public class TabsData {

        private String title;
        private Drawable drawable;

        public TabsData(String title, Drawable drawableId) {
            this.title = title;
            this.drawable = drawableId;
        }

        public String getTitle() {
            return title;
        }

        public Drawable getDrawable() {
            return drawable;
        }
    }

}
