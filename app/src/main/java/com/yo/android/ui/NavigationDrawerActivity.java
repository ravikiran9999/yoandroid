package com.yo.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yo.android.R;
import com.yo.android.adapters.MenuListAdapter;
import com.yo.android.adapters.TabsPagerAdapter;
import com.yo.android.chat.ui.fragments.ChatFragment;
import com.yo.android.chat.ui.fragments.ContactsFragment;
import com.yo.android.model.MenuData;
import com.yo.android.ui.fragments.CallFragment;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.voip.SipService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ramesh on 23/6/16.
 */
public class NavigationDrawerActivity extends BaseActivity {
    private DrawerLayout mDrawerLayout;

    private NavigationView navigationView;

    private ActionBarDrawerToggle mDrawerToggle;

    private Toolbar toolbar;

    protected MenuListAdapter menuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mDrawerLayout.getWindowToken(), 0);
            }
        };
        mDrawerToggle.syncState();
        //
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
        prepareNavigationDrawerOptions();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new MagazinesFragment(), "MAGAZINES");
        mAdapter.addFragment(new CallFragment(), "CALLS");
        mAdapter.addFragment(new ChatFragment(), "CHATS");
        mAdapter.addFragment(new ContactsFragment(), "CONTACTS");
        viewPager.setAdapter(mAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        //
        //
        Intent in = new Intent(getApplicationContext(), SipService.class);
        startService(in);


    }

    public void prepareNavigationDrawerOptions() {

        menuAdapter = new MenuListAdapter(this);
        ListView menuListView = (ListView) findViewById(R.id.menuListView);
        menuAdapter.addItems(getMenuList());

        menuListView.setAdapter(menuAdapter);
        menuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        menuListView.setSelection(0);
        menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawers();
                MenuData menuData = (MenuData) menuAdapter.getItem(position);
                if ("Dialer".equalsIgnoreCase(menuData.getName())) {
                    startActivity(new Intent(NavigationDrawerActivity.this, DialerActivity.class));
                } else if ("Settings".equalsIgnoreCase(menuData.getName())) {
                    Intent settingsIntent = new Intent(NavigationDrawerActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                }
            }
        });
    }

    public List<MenuData> getMenuList() {
        List<MenuData> menuDataList = new ArrayList<>();
        menuDataList.add(new MenuData("Dialer", R.drawable.ic_menu_settings));
        menuDataList.add(new MenuData("Magazines", R.drawable.ic_menu_settings));
        menuDataList.add(new MenuData("Broadcast", R.drawable.ic_menu_settings));
        menuDataList.add(new MenuData("Wallet", R.drawable.ic_menu_settings));
        menuDataList.add(new MenuData("Invite Friends", R.drawable.ic_menu_settings));
        menuDataList.add(new MenuData("Notifications", R.drawable.ic_menu_settings));
        menuDataList.add(new MenuData("Profile", R.drawable.ic_menu_settings));
        menuDataList.add(new MenuData("Settings", R.drawable.ic_menu_settings));
        menuDataList.add(new MenuData("Logout", R.drawable.ic_menu_settings));
        return menuDataList;
    }

    public void setToolBarTitle(String title) {
        toolbar.setTitle(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getSupportFragmentManager().findFragmentById(R.id.content) != null) {
            getSupportFragmentManager().findFragmentById(R.id.content).onActivityResult(requestCode, resultCode, data);
        }
    }

}
