package com.yo.android.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.adapters.TabsPagerAdapter;
import com.yo.android.chat.notification.helper.NotificationCache;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.Popup;
import com.yo.android.receiver.NetworkChangeReceiver;
import com.yo.android.typeface.CustomTypefaceSpan;
import com.yo.android.ui.fragments.NewCreditAccountFragment;
import com.yo.android.ui.fragments.RechargeDetailsFragment;
import com.yo.android.ui.fragments.SpendDetailsFragment;
import com.yo.android.util.Constants;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.vox.BalanceHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;


import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

import static com.yo.android.flip.MagazineFlipArticlesFragment.updateCalled;

public class TabsHeaderActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener, ViewPager.OnPageChangeListener {

    public static final String CREDIT_ACCOUNT = "CREDIT ACCOUNT";
    public static final String RECHARGE_DETAILS = "RECHARGE DETAILS";
    public static final String SPEND_DETAILS = "SPEND DETAILS";

    private boolean isAlreadyShown;
    //private boolean isRemoved;
    private boolean isSharedPreferenceShown;
    private boolean isRenewal;
    private TabsPagerAdapter adapter;
    private Menu menu;

    /*@BindView(R.id.network_status_view)
    ImageView networkView;*/

    @Inject
    BalanceHelper mBalanceHelper;

    @BindView(R.id.your_available_amount)
    TextView balanceText;
    @BindView(R.id.htab_toolbar)
    Toolbar toolbar;
    @BindView(R.id.title)
    TextView titleView;
    @BindView(R.id.htab_viewpager)
    ViewPager viewPager;
    @BindView(R.id.htab_tabs)
    TabLayout tabLayout;


    NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_yo_credit_screen);
        ButterKnife.bind(this);

        balanceText.setText(spannableString());

        updateCalled = 0;
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            if (getIntent().hasExtra(Constants.OPEN_ADD_BALANCE)) {
                //getSupportActionBar().setTitle(R.string.add_balance_title);
                titleView.setText(R.string.add_balance_title);
            } else {
                //getSupportActionBar().setTitle(spannableStringTitle(R.string.yo_credit));
                titleView.setText(spannableStringTitle(R.string.yo_credit));
            }
        }


        isRenewal = getIntent().getBooleanExtra(Constants.RENEWAL, false);
        enableBack();

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        // Collapsing toolbar
        if (!BuildConfig.NEW_YO_CREDIT_SCREEN) {
            final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.htab_collapse_toolbar);
            collapsingToolbarLayout.setTitleEnabled(false);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.patan);

            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @SuppressWarnings("ResourceType")
                @Override
                public void onGenerated(Palette palette) {
                    int vibrantColor = palette.getVibrantColor(R.color.colorPrimary);
                    int vibrantDarkColor = palette.getDarkVibrantColor(R.color.colorPrimaryDark);
                    collapsingToolbarLayout.setContentScrimColor(vibrantColor);
                    collapsingToolbarLayout.setStatusBarScrimColor(vibrantDarkColor);
                }
            });

        }

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

        if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
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

        EventBus.getDefault().register(this);
        networkChangeReceiver = new NetworkChangeReceiver(this);
        registerBroadCastReceiver();
    }


    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new TabsPagerAdapter(getSupportFragmentManager());
        Fragment fragment;
        fragment = new NewCreditAccountFragment();
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
        adapter.addFragment(fragment, CREDIT_ACCOUNT);
        if (!getIntent().hasExtra(Constants.OPEN_ADD_BALANCE)) {
            adapter.addFragment(new RechargeDetailsFragment(), RECHARGE_DETAILS);
            adapter.addFragment(new SpendDetailsFragment(), SPEND_DETAILS);
        }
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
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
        unregisterBroadcastReceiver();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void closePopup() {
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        if (popup != null) {
            if (!isSharedPreferenceShown) {
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

    public void onEventMainThread(String action) {
        if (Constants.BALANCE_UPDATED_ACTION.equals(action) && balanceText != null) {
            //balanceText.setText(String.format(getString(R.string.your_yo_balance), mBalanceHelper.getCurrentBalance()));
            balanceText.setText(spannableString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private SpannableStringBuilder spannableString() {
        Typeface alexBrushRegular = getAlexBrushRegular();
        TypefaceSpan alexBrushRegularSpan = new CustomTypefaceSpan("", alexBrushRegular);
        String amount = mBalanceHelper.getCurrentBalance();
        String yoBalanceString = String.format(getString(R.string.your_yo_balance), mBalanceHelper.currencySymbolLookup(amount));
        final SpannableStringBuilder text = new SpannableStringBuilder(yoBalanceString);
        // Span to make text bold
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);

        text.setSpan(alexBrushRegularSpan, 6, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        text.setSpan(new RelativeSizeSpan(2f), 6, 9, 0); // set size
        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), 17, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // make them also bold
        text.setSpan(bss, 17, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return text;
    }

    private SpannableStringBuilder spannableStringTitle(int title) {
        String mTitle = getResources().getString(title);
        Typeface alexBrushRegular = getAlexBrushRegular();
        TypefaceSpan alexBrushRegularSpan = new CustomTypefaceSpan("", alexBrushRegular);
        final SpannableStringBuilder text = new SpannableStringBuilder(mTitle);

        text.setSpan(alexBrushRegularSpan, 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        text.setSpan(new RelativeSizeSpan(2f), 0, 3, 0); // set size

        return text;
    }


    private void registerBroadCastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.registerReceiver(networkChangeReceiver, new IntentFilter(intentFilter));
    }

    public void unregisterBroadcastReceiver() {
        this.unregisterReceiver(networkChangeReceiver);
    }

    public void showNetworkStatus(int status) {
        final int sdk = android.os.Build.VERSION.SDK_INT;
        if (status == 1) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                balanceText.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.white_circle_with_green_status));
            } else {
                balanceText.setBackground(ContextCompat.getDrawable(this, R.drawable.white_circle_with_green_status));
            }
        } else {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                balanceText.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.white_circle_with_red_status));
            } else {
                balanceText.setBackground(ContextCompat.getDrawable(this, R.drawable.white_circle_with_red_status));
            }
        }
    }

}