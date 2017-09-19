package com.yo.android.ui;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yo.android.R;
import com.yo.android.adapters.TabsPagerAdapter;
import com.yo.android.chat.notification.helper.NotificationCache;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.Popup;
import com.yo.android.ui.fragments.CreditAccountFragment;
import com.yo.android.ui.fragments.RechargeDetailsFragment;
import com.yo.android.ui.fragments.SpendDetailsFragment;
import com.yo.android.util.Constants;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yo.android.flip.MagazineFlipArticlesFragment.updateCalled;

public class TabsHeaderActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener, ViewPager.OnPageChangeListener {

    private boolean isAlreadyShown;
    //private boolean isRemoved;
    private boolean isSharedPreferenceShown;
    private boolean isRenewal;
    private ViewPager viewPager;
    private TabsPagerAdapter adapter;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.actvity_yo_credit);
        updateCalled = 0;
        final Toolbar toolbar = (Toolbar) findViewById(R.id.htab_toolbar);
        setSupportActionBar(toolbar);
        if (getIntent().hasExtra(Constants.OPEN_ADD_BALANCE)) {
            getSupportActionBar().setTitle(R.string.add_balance_title);
        } else {
            getSupportActionBar().setTitle(R.string.yo_credit);
        }
        isRenewal = getIntent().getBooleanExtra(Constants.RENEWAL, false);
        enableBack();
        viewPager = (ViewPager) findViewById(R.id.htab_viewpager);
        setupViewPager(viewPager);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.htab_tabs);
        tabLayout.setupWithViewPager(viewPager);

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.htab_collapse_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.patan);

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @SuppressWarnings("ResourceType")
            @Override
            public void onGenerated(Palette palette) {
                int vibrantColor = palette.getVibrantColor(R.color.primary_500);
                int vibrantDarkColor = palette.getDarkVibrantColor(R.color.primary_700);
                collapsingToolbarLayout.setContentScrimColor(vibrantColor);
                collapsingToolbarLayout.setStatusBarScrimColor(vibrantDarkColor);
            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        preferenceEndPoint.saveIntPreference(Constants.NOTIFICATION_COUNT, 0);
        NotificationCache.clearNotifications();

        if(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
            Type type = new TypeToken<List<Popup>>() {
            }.getType();
            List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
            if (popup != null) {
                Collections.reverse(popup);
                isAlreadyShown = false;
                for (Popup p : popup) {
                    if (p.getPopupsEnum() == PopupHelper.PopupsEnum.YOCREDIT) {
                        if (!isAlreadyShown) {
                            PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.YOCREDIT, p, this, preferenceEndPoint, null, this, popup);
                            isAlreadyShown = true;
                            isSharedPreferenceShown = false;
                            break;
                        }
                    }
                }
            }
        }
    }


    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new TabsPagerAdapter(getSupportFragmentManager());
        Fragment fragment = new CreditAccountFragment();
        if (getIntent().hasExtra(Constants.OPEN_ADD_BALANCE)) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.OPEN_ADD_BALANCE, true);
            fragment.setArguments(bundle);
        }
        if (getIntent().hasExtra(Constants.RENEWAL)) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.RENEWAL, isRenewal);
            fragment.setArguments(bundle);
        }
        adapter.addFragment(fragment, "Credit Account");
        if (!getIntent().hasExtra(Constants.OPEN_ADD_BALANCE)) {
            adapter.addFragment(new RechargeDetailsFragment(), "Recharge Details");
            adapter.addFragment(new SpendDetailsFragment(), "Spend Details");
        }
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                Type type = new TypeToken<List<Popup>>() {
                }.getType();
                List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                if (popup != null) {
                    for (Popup p : popup) {
                        if (p.getPopupsEnum() == PopupHelper.PopupsEnum.YOCREDIT) {
                            if (!isAlreadyShown) {
                                PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.YOCREDIT, p, this, preferenceEndPoint, null, this, popup);
                                isAlreadyShown = true;
                                isSharedPreferenceShown = true;
                                break;
                            }
                        }
                    }
                }
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void closePopup() {
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        if (popup != null) {
            if(!isSharedPreferenceShown) {
                Collections.reverse(popup);
            }
            List<Popup> tempPopup = new ArrayList<>(popup);
            for (Popup p : popup) {
                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.YOCREDIT) {
                    tempPopup.remove(p);
                    break;
                }
            }
            popup = tempPopup;
        }
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //DatePickerActivity.startForResult(this, 1001, new DateCalendar("20/06/2017"), new DateCalendar("20/06/2017"), "departure", false, "oneway");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Fragment fragment = adapter.getItem(position);

        // menu in spend details screen
        /*if (fragment instanceof SpendDetailsFragment) {
            getMenuInflater().inflate(R.menu.filter, menu);
        } else {
            menu.clear();
        }*/
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}