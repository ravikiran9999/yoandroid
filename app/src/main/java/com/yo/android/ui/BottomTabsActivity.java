package com.yo.android.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.yo.android.R;
import com.yo.android.adapters.TabsPagerAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.firebase.FirebaseService;
import com.yo.android.chat.firebase.MyServiceConnection;
import com.yo.android.chat.notification.helper.NotificationCache;
import com.yo.android.chat.notification.pojo.UserData;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.chat.ui.fragments.ChatFragment;
import com.yo.android.chat.ui.fragments.ContactsFragment;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.model.Articles;
import com.yo.android.model.FindPeople;
import com.yo.android.model.NotificationCount;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.pjsip.SipBinder;
import com.yo.android.pjsip.SipProfile;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.sync.SyncUtils;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.InviteActivity;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.ui.fragments.MoreFragment;
import com.yo.android.util.Constants;
import com.yo.android.util.ContactSyncHelper;
import com.yo.android.util.Util;
import com.yo.android.voip.SipService;
import com.yo.android.vox.BalanceHelper;
import com.yo.android.widgets.CustomViewPager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 3/7/16.
 */
public class BottomTabsActivity extends BaseActivity {

    //private Toolbar toolbar;
    private TabLayout tabLayout;
    private List<TabsData> dataList;
    @Inject
    BalanceHelper balanceHelper;
    TabsPagerAdapter mAdapter;
    public CustomViewPager viewPager;
    @Inject
    ContactsSyncManager contactsSyncManager;
    @Inject
    MyServiceConnection myServiceConnection;
    @Inject
    ContactSyncHelper mContactSyncHelper;
    private Button notificationCount;
    private ViewGroup customActionBar;
    private Context context;
    private SipBinder sipBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sipBinder = (SipBinder) service;
            addAccount();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sipBinder = null;
        }
    };

    private void addAccount() {
        String username = preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME, null);
        String password = preferenceEndPoint.getStringPreference(Constants.PASSWORD, null);
        SipProfile sipProfile = new SipProfile.Builder()
                .withUserName(username == null ? "" : username)
                .withPassword("123456")
                .withServer("209.239.120.239")
                .build();
        sipBinder.getHandler().addAccount(sipProfile);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_tabs);
        // toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        context = this;

        preferenceEndPoint.saveBooleanPreference(Constants.IS_IN_APP, true);

        viewPager = (CustomViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new MagazinesFragment(), null);
        mAdapter.addFragment(new ChatFragment(), null);
        mAdapter.addFragment(new DialerFragment(), null);
        mAdapter.addFragment(new ContactsFragment(), null);
        mAdapter.addFragment(new MoreFragment(), null);
        viewPager.setAdapter(mAdapter);

        preferenceEndPoint.saveBooleanPreference("isNotifications", false);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        dataList = createTabsList();
        int index = 0;
        for (TabsData data : dataList) {
            final TabLayout.Tab tab = tabLayout.getTabAt(index);
            tab.setCustomView(setTabs(data.getTitle(), data.getDrawable()));
            index++;
        }

        customActionBar = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_action_bar, null);
        notificationCount = (Button) customActionBar.findViewById(R.id.notif_count);
        ImageView notificationEnable = (ImageView) customActionBar.findViewById(R.id.yo_icon);
        notificationEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, NotificationsActivity.class));
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                try {
                    Util.closeSearchView(getMenu());
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                    getSupportActionBar().setDisplayShowCustomEnabled(true);
                    getSupportActionBar().setCustomView(customActionBar);

                    if (getFragment() instanceof MoreFragment) {
                        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.profile_background));
                    } else {
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                    }
                    TextView actionBarTitle = (TextView) customActionBar.findViewById(R.id.action_bar_title);
                    actionBarTitle.setText((dataList.get(position)).getTitle());

                } catch (NullPointerException e) {
                    mLog.w("onPageSelected", e);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // firebase service

        if (myServiceConnection != null && !myServiceConnection.isServiceConnection()) {
            Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
            startService(intent);
            bindService(intent, myServiceConnection, Context.BIND_AUTO_CREATE);
        }

        //
        Intent in = new Intent(getApplicationContext(), SipService.class);
        startService(in);
        balanceHelper.checkBalance(null);
        //
        loadUserProfileInfo();
        updateDeviceToken();
        //contactsSyncManager.syncContacts();
        SyncUtils.createSyncAccount(this, preferenceEndPoint);
        mContactSyncHelper.checkContacts();
        bindService(new Intent(this, YoSipService.class), connection, BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);

        List<UserData> notificationList = NotificationCache.get().getCacheNotifications();
        if (notificationList.size() == 1) {
            Intent intent1 = getIntent();
            String title = intent1.getStringExtra("title");
            String message = intent1.getStringExtra("message");
            String tag = intent1.getStringExtra("tag");
            final String redirectId = intent1.getStringExtra("id");

            if (!("POPUP").equals(tag)) {
                if ("User".equals(tag)) {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.getUserInfoFromId(redirectId, accessToken).enqueue(new Callback<FindPeople>() {
                        @Override
                        public void onResponse(Call<FindPeople> call, Response<FindPeople> response) {

                            if (response.body() != null) {
                                FindPeople userInfo = response.body();
                                Intent intent = new Intent(BottomTabsActivity.this, OthersProfileActivity.class);
                                intent.putExtra(Constants.USER_ID, redirectId);
                                intent.putExtra("PersonName", userInfo.getFirst_name() + " " + userInfo.getLast_name());
                                intent.putExtra("PersonPic", userInfo.getAvatar());
                                intent.putExtra("PersonIsFollowing", userInfo.getIsFollowing());
                                intent.putExtra("MagazinesCount", userInfo.getMagzinesCount());
                                intent.putExtra("FollowersCount", userInfo.getFollowersCount());
                                intent.putExtra("LikedArticlesCount", userInfo.getLikedArticlesCount());
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<FindPeople> call, Throwable t) {

                        }
                    });

                } else if ("Topic".equals(tag)) {
                    Intent intent = new Intent(this, MyCollectionDetails.class);
                    intent.putExtra("TopicId", redirectId);
                    intent.putExtra("TopicName", title);
                    intent.putExtra("Type", "Tag");
                    startActivity(intent);
                    finish();
                } else if ("Article".equals(tag)) {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.getArticleInfo(redirectId, accessToken).enqueue(new Callback<Articles>() {
                        @Override
                        public void onResponse(Call<Articles> call, Response<Articles> response) {
                            if (response.body() != null) {
                                Articles articles = response.body();
                                Intent intent = new Intent(BottomTabsActivity.this, MagazineArticleDetailsActivity.class);
                                intent.putExtra("Title", articles.getTitle());
                                intent.putExtra("Image", articles.getUrl());
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<Articles> call, Throwable t) {

                        }
                    });

                } else if ("Magzine".equals(tag)) {
                    Intent intent = new Intent(this, MyCollectionDetails.class);
                    intent.putExtra("TopicId", redirectId);
                    intent.putExtra("TopicName", title);
                    intent.putExtra("Type", "Magzine");
                    startActivity(intent);
                    finish();
                } else if ("Recharge".equals(tag) || "Credit".equals(tag) || "BalanceTransferred".equals(tag)) {
                    startActivity(new Intent(this, TabsHeaderActivity.class));
                    finish();
                } else if ("Broadcast".equals(tag) || "Tip".equals(tag) || "PriceUpdate".equals(tag)) {
                    if (redirectId.equals("AddFriends")) {
                        startActivity(new Intent(this, InviteActivity.class));
                        finish();
                    } else if (redirectId.equals("AddBalance")) {
                        startActivity(new Intent(this, TabsHeaderActivity.class));
                        finish();
                    }

                } else if ("Missed call".equals(tag)) {
                    //startActivity(new Intent(this, DialerActivity.class));
                    viewPager.setCurrentItem(2);
                }
            }
        }

        Intent intent = getIntent();
        if(intent.hasExtra("type")) {
            if("Missed call".equals(intent.getStringExtra("type").trim())) {
                viewPager.setCurrentItem(2);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getFragment();
        boolean handle = fragment instanceof BaseFragment && ((BaseFragment) fragment).onBackPressHandle();
        if (handle) {
            mLog.i("BottomTabsActivity", "Back button handled");
        } else {
            super.onBackPressed();
        }
    }

    public Fragment getFragment() {
        int position = tabLayout.getSelectedTabPosition();
        return mAdapter.getItem(position);
    }

    /*public void setToolBarTitle(String title) {
        final TextView titleView = (TextView) toolbar.findViewById(R.id.title);
        titleView.setText(title);
    }*/

    public void showOrHideTabs(boolean show) {
        viewPager.setPagingEnabled(show);
        if (show) {
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getFragment() != null) {
            getFragment().onActivityResult(requestCode, resultCode, data);
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
        list.add(new TabsData(getString(R.string.magazines), getResources().getDrawable(R.drawable.tab_magazines)));
        list.add(new TabsData(getString(R.string.chats), getResources().getDrawable(R.drawable.tab_chats)));
        list.add(new TabsData(getString(R.string.dialer), getResources().getDrawable(R.drawable.tab_dialer)));
        list.add(new TabsData(getString(R.string.contacts), getResources().getDrawable(R.drawable.tab_contacts)));
        list.add(new TabsData(getResources().getString(R.string.profile), getResources().getDrawable(R.drawable.tab_more)));
        return list;
    }

    public Drawable createStateList(int normal, int selected) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{}, getResources().getDrawable(selected));
        states.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(normal));
        return states;
    }

    /*public void setToolBarColor(int toolBarColor) {
        this.toolbar.setBackgroundColor(toolBarColor);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_search == item.getItemId()) {
            //setToolBarColor(getResources().getColor(R.color.colorPrimary));

            Menu menu1 = getMenu();
            if (menu1 != null) {
                Util.changeMenuItemsVisibility(menu1, R.id.menu_search, false);
                Util.registerSearchLister(this, menu1);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private Menu getMenu() {
        Menu menu = null;
        if (getFragment() instanceof ChatFragment) {
            menu = ((ChatFragment) getFragment()).getMenu();
        } else if (getFragment() instanceof ContactsFragment) {
            menu = ((ContactsFragment) getFragment()).getMenu();
        } else if (getFragment() instanceof DialerFragment) {
            menu = ((DialerFragment) getFragment()).getMenu();
        } else if (getFragment() instanceof MagazinesFragment) {
            menu = ((MagazinesFragment) getFragment()).getMenu();
        }
        return menu;
    }

    public void refresh() {
        if (getFragment() instanceof MagazinesFragment) {
            ((MagazinesFragment) getFragment()).getmMagazineFlipArticlesFragment().refresh();
        }
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

    private void loadUserProfileInfo() {
        String access = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.getUserInfo(access).enqueue(new Callback<UserProfileInfo>() {
            @Override
            public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                if (response.body() != null) {
                    Util.saveUserDetails(response, preferenceEndPoint);
                    preferenceEndPoint.saveStringPreference(Constants.USER_ID, response.body().getId());
                    preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
                    preferenceEndPoint.saveStringPreference(Constants.USER_STATUS, response.body().getDescription());
                    preferenceEndPoint.saveStringPreference(Constants.FIREBASE_USER_ID, response.body().getFirebaseUserId());
                    if (TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.USER_NAME))) {
                        preferenceEndPoint.saveStringPreference(Constants.USER_NAME, response.body().getFirstName());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {

            }
        });
    }

    private void updateDeviceToken() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (!TextUtils.isEmpty(refreshedToken)) {
            preferenceEndPoint.saveStringPreference(Constants.FCM_REFRESH_TOKEN, refreshedToken);
        } else {
            refreshedToken = preferenceEndPoint.getStringPreference(Constants.FCM_REFRESH_TOKEN);
        }
        if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.updateDeviceTokenAPI(accessToken, refreshedToken).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }

    public void onEventMainThread(String action) {
        if (Constants.BALANCE_TRANSFER_NOTIFICATION_ACTION.equals(action)) {
            if (balanceHelper != null) {
                showProgressDialog();
                balanceHelper.checkBalance(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        dismissProgressDialog();
                        try {
                            DecimalFormat df = new DecimalFormat("0.000");
                            String format = df.format(Double.valueOf(balanceHelper.getCurrentBalance()));
                            preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, format);
                        } catch (IllegalArgumentException e) {
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        dismissProgressDialog();

                    }
                });
            }
        }
    }

    public void onEventMainThread(NotificationCount count) {
        if (count.getCount() > 0) {
            update(count.getCount());
        }
    }

    public void update(final int value) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notificationCount.setVisibility(View.VISIBLE);
                String valueCount = value > 99 ? "99+" : String.valueOf(value);
                notificationCount.setText(valueCount);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferenceEndPoint.getIntPreference(Constants.NOTIFICATION_COUNT) == 0) {
            notificationCount.setVisibility(View.GONE);
        } else if (preferenceEndPoint.getIntPreference(Constants.NOTIFICATION_COUNT) > 0) {
            update(preferenceEndPoint.getIntPreference(Constants.NOTIFICATION_COUNT));
        }
    }
}
