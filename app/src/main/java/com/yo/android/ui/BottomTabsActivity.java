package com.yo.android.ui;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.WebserviceUsecase;
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
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.Helper;
import com.yo.android.model.Articles;
import com.yo.android.model.FindPeople;
import com.yo.android.model.NotificationCount;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.pjsip.SipBinder;
import com.yo.android.pjsip.YoSipService;
import com.yo.android.sync.SyncUtils;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.ui.fragments.InviteActivity;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.ui.fragments.MoreFragment;
import com.yo.android.ui.fragments.NewContactsFragment;
import com.yo.android.util.Constants;
import com.yo.android.util.ContactSyncHelper;
import com.yo.android.util.FetchNewArticlesService;
import com.yo.android.util.MagazineDashboardHelper;
import com.yo.android.util.Util;
import com.yo.android.voip.SipService;
import com.yo.android.vox.BalanceHelper;
import com.yo.android.widgets.CustomViewPager;
import com.yo.dialer.CallExtras;
import com.yo.dialer.DialerLogs;
import com.yo.dialer.NewDialerFragment;
import com.yo.dialer.googlesheet.UploadCallDetails;
import com.yo.dialer.googlesheet.UploadModel;
import com.yo.restartapp.YOExceptionHandler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yo.dialer.googlesheet.UploadCallDetails.SCOPES;

/**
 * Created by Ramesh on 3/7/16.
 */
public class BottomTabsActivity extends BaseActivity {

    private static final String TAG = BottomTabsActivity.class.getSimpleName();
    private TabLayout tabLayout;
    private List<TabsData> dataList;
    @Inject
    BalanceHelper balanceHelper;
    @Inject
    ContactsSyncManager contactsSyncManager;
    @Inject
    MyServiceConnection myServiceConnection;
    @Inject
    ContactSyncHelper mContactSyncHelper;
    @Inject
    WebserviceUsecase webserviceUsecase;

    TabsPagerAdapter mAdapter;
    public CustomViewPager viewPager;
    private Button notificationCount;
    private ImageView notificationEnable;
    private ViewGroup customActionBar;
    private Context context;
    private SipBinder sipBinder;
    private static Context mContext;
    public static Activity activity;
    public static PendingIntent pintent;
    private TextView actionBarTitle;
    private SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final int REQUEST_AUDIO_RECORD = 200;
    private int lastFragmentPosition = 0;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sipBinder = (SipBinder) service;
            /*if (!sipBinder.getYOHandler().isOnGOingCall()) {
                clearNotifications();
            }*/
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sipBinder = null;
        }
    };

    public static GoogleAccountCredential mCredential;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_tabs);
        context = this;
        activity = this;
        mContext = getApplicationContext();

        // TODO: Test
        Intent service = new Intent(this, com.yo.dialer.YoSipService.class);
        service.setAction(CallExtras.REGISTER);
        startService(service);


        // Handle application crash
        Thread.setDefaultUncaughtExceptionHandler(new YOExceptionHandler(this));
        if (getIntent().getBooleanExtra("crash", false)) {
            clearNotifications();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.USE_SIP,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.CAMERA,
                    //Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.GET_TASKS,
                    Manifest.permission.WRITE_SYNC_SETTINGS,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CHANGE_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,

            }, REQUEST_AUDIO_RECORD);
        } else {
            if (mCredential == null) {
                mCredential = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Arrays.asList(SCOPES))
                        .setBackOff(new ExponentialBackOff());
                UploadCallDetails.getInstance(this).getResultsFromApi(this, mCredential);
            }
        }

        preferenceEndPoint.saveBooleanPreference(Constants.IS_IN_APP, true);
        preferenceEndPoint.saveBooleanPreference(Constants.LAUNCH_APP, true);

        viewPager = (CustomViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new MagazinesFragment(), null);
        mAdapter.addFragment(new ChatFragment(), null);
        if (BuildConfig.NEW_DIALER) {
            mAdapter.addFragment(new NewDialerFragment(), null);
        } else {
            mAdapter.addFragment(new DialerFragment(), null);
        }
        if (BuildConfig.NEW_CONTACTS_SCREEN) {
            mAdapter.addFragment(new NewContactsFragment(), null);
        } else {
            mAdapter.addFragment(new ContactsFragment(), null);
        }
        mAdapter.addFragment(new MoreFragment(), null);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mAdapter);

        preferenceEndPoint.saveBooleanPreference("isNotifications", false);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        dataList = createTabsList();
        int index = 0;
        for (TabsData data : dataList) {
            final TabLayout.Tab tab = tabLayout.getTabAt(index);
            if(tab != null) {
                tab.setCustomView(setTabs(data.getTitle(), data.getDrawable()));
            }
            index++;
        }

        customActionBar = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_action_bar, null);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setCustomView(customActionBar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }

        notificationCount = (Button) customActionBar.findViewById(R.id.notif_count);
        notificationEnable = (ImageView) customActionBar.findViewById(R.id.yo_icon);
        notificationEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, NotificationsActivity.class));
            }
        });
        actionBarTitle = (TextView) customActionBar.findViewById(R.id.action_bar_title);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                try {
                    Util.closeSearchView(getMenu());


                    actionBarTitle.setText((dataList.get(position)).getTitle());

                    if (getFragment() instanceof MoreFragment) {
                        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.profile_background));
                        getSupportActionBar().setElevation(0);
                        balanceHelper.checkBalance(null);
                    } else {
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                    }
                } catch (NullPointerException e) {
                    if (mLog != null) {
                        mLog.w("onPageScrolled", e);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {

                /*switch (position) {
                    case 0:
                        if (getFragment() instanceof MagazinesFragment) {
                            MagazineDashboardHelper.request = 1;
                            ((MagazinesFragment) getFragment()).removeReadArticles();
                            ((MagazinesFragment) getFragment()).update();
                            MagazineFlipArticlesFragment.currentFlippedPosition = 0;
                        }
                        break;
                    case 2:
                        if (getFragment() instanceof DialerFragment) {
                            ((DialerFragment) getFragment()).loadData();
                        }
                }*/


                if (lastFragmentPosition == 0) {
                    mLog.d(TAG, "Leaving Magazines tab");
                    // End the timed event, when the user navigates away from Magazines tab
                    FlurryAgent.endTimedEvent("Magazines");
                    lastFragmentPosition = position;
                } else if (lastFragmentPosition == 1) {
                    mLog.d(TAG, "Leaving Chats tab");
                    // End the timed event, when the user navigates away from Chats tab
                    FlurryAgent.endTimedEvent("Chats");
                    lastFragmentPosition = position;
                } else if (lastFragmentPosition == 2) {
                    mLog.d(TAG, "Leaving Dialer tab");
                    // End the timed event, when the user navigates away from Dialer tab
                    FlurryAgent.endTimedEvent("Dialer");
                    lastFragmentPosition = position;
                } else if (lastFragmentPosition == 3) {
                    mLog.d(TAG, "Leaving Contacts tab");
                    // End the timed event, when the user navigates away from Contacts tab
                    FlurryAgent.endTimedEvent("Contacts");
                    lastFragmentPosition = position;
                } else if (lastFragmentPosition == 4) {
                    mLog.d(TAG, "Leaving Profile tab");
                    // End the timed event, when the user navigates away from Profile tab
                    FlurryAgent.endTimedEvent("Profile");
                    lastFragmentPosition = position;
                }

                if (position == 0 && getFragment() instanceof MagazinesFragment) {
                    Log.d(TAG, "onPageSelected In update() BottomTabsActivity");

                    MagazineDashboardHelper.request = 1;
                    ((MagazinesFragment) getFragment()).removeReadArticles();
                    ((MagazinesFragment) getFragment()).update();
                    MagazineFlipArticlesFragment.currentFlippedPosition = 0;


                }

            }
            @Override
            public void onPageScrollStateChanged ( int state){

            }
        });

        if(!preferenceEndPoint.getBooleanPreference(Constants.IS_SERVICE_RUNNING))

            {
                int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
                if (currentHour == 0) {
                    startServiceToFetchNewArticles();
                }
            }

            // firebase service

        if(myServiceConnection !=null&&!myServiceConnection.isServiceConnection())

            {
                //bindService(intent, myServiceConnection, Context.BIND_AUTO_CREATE);
            }

            //
            Intent in = new Intent(getApplicationContext(), SipService.class);

            startService(in);

            //balanceHelper.checkBalance(null);
            //
            loadUserProfileInfo();

            updateDeviceToken();
            //contactsSyncManager.syncContacts();
        SyncUtils.createSyncAccount(BottomTabsActivity.this,preferenceEndPoint);
        mContactSyncHelper.init();
        mContactSyncHelper.checkContacts();

            bindService(new Intent(BottomTabsActivity.this, YoSipService.class),connection,BIND_AUTO_CREATE);
        EventBus.getDefault().

            register(this);

            List<UserData> notificationList = NotificationCache.get().getCacheNotifications();

            Intent intent1 = getIntent();
        if(!intent1.getBooleanExtra("fromLowBalNotification",false))

            {
                balanceHelper.checkBalance(null);
            }

            String tag = intent1.getStringExtra("tag");
        if(notificationList.size()==1)

            {
                String title = intent1.getStringExtra("title");
                String message = intent1.getStringExtra("message");
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
                        Intent intent = new Intent(BottomTabsActivity.this, MyCollectionDetails.class);
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
                                    intent.putExtra("Article", articles);
                                    intent.putExtra("Position", 0);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(Call<Articles> call, Throwable t) {

                            }
                        });

                    } else if ("Magzine".equals(tag)) {
                        Intent intent = new Intent(BottomTabsActivity.this, MyCollectionDetails.class);
                        intent.putExtra("TopicId", redirectId);
                        intent.putExtra("TopicName", title);
                        intent.putExtra("Type", "Magzine");
                        startActivity(intent);
                        finish();
                    } else if ("Recharge".equals(tag) || "Credit".equals(tag) || "BalanceTransferred".equals(tag)) {
                        startActivity(new Intent(BottomTabsActivity.this, TabsHeaderActivity.class));
                        finish();
                    } else if ("Broadcast".equals(tag) || "Tip".equals(tag) || "PriceUpdate".equals(tag)) {
                        if (Constants.ADDFRIENDS.equals(redirectId)) {
                            startActivity(new Intent(BottomTabsActivity.this, InviteActivity.class));
                            finish();
                        } else if (Constants.ADDBALANCE.equals(redirectId)) {
                            startActivity(new Intent(BottomTabsActivity.this, TabsHeaderActivity.class));
                            finish();
                        }

                    } else if ("Missed call".equals(tag)) {
                        //startActivity(new Intent(this, DialerActivity.class));
                        viewPager.setCurrentItem(2);
                    }
                }
            } else

            {
                if ("Recharge".equals(tag) || "Credit".equals(tag) || "BalanceTransferred".equals(tag)) {

                    startActivity(new Intent(BottomTabsActivity.this, TabsHeaderActivity.class));
                    finish();
                }
            }

            Intent intent = getIntent();
        if(intent.hasExtra("type"))

            {
                if ("Missed call".equals(intent.getStringExtra("type").trim())) {
                    viewPager.setCurrentItem(2);
                }
            }

            // Capture user id
            Map<String, String> appUsageParams = new HashMap<String, String>();
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            //param keys and values have to be of String type
        appUsageParams.put("UserId",userId);

        FlurryAgent.logEvent("Opened Yo App",appUsageParams);

            // Test.startInComingCallScreen(context);
        }

    private void clearNotifications() {
        NotificationCache.get().clearNotifications();
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    @Override


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        DialerLogs.messageI(TAG, requestCode + " Permissions while requesting " + grantResults[2]);
       /* mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(UploadCallDetails.SCOPES))
                .setBackOff(new ExponentialBackOff());*/


        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        UploadCallDetails.getInstance(this).getResultsFromApi(this, mCredential);

        if (requestCode == REQUEST_AUDIO_RECORD) {
            if (grantResults[2] == PackageManager.PERMISSION_GRANTED && permissions[2] == Manifest.permission.GET_ACCOUNTS) {
                DialerLogs.messageI(TAG, permissions.length + " Permissions and initializing google drive ");


                //start audio recording or whatever you planned to do
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(BottomTabsActivity.this, Manifest.permission.RECORD_AUDIO)) {
                    //Show an explanation to the user *asynchronously*
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("This permission is important to record audio.")
                            .setTitle("Important permission required");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(BottomTabsActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_RECORD);
                        }
                    });
                    ActivityCompat.requestPermissions(BottomTabsActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_RECORD);
                } else {
                    //Never ask again and handle your app without permission.
                }
            }
        }
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
        int position = 0;
        if (tabLayout != null) {
            position = tabLayout.getSelectedTabPosition();
            return mAdapter.getItem(position);
        }
        return null;

        // Todo changes from enhancement branch
        /*if(tabLayout != null && mAdapter != null) {
            int position = tabLayout.getSelectedTabPosition();
            return mAdapter.getItem(position);
        } else {
            return mAdapter.getItem(0);
        }*/
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

        switch (requestCode) {
            // user has  returned back from the account picker,
            // initiate the rest of the flow with the account he/she has chosen.
            case UploadCallDetails.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        if (accountName != null) {
                            SharedPreferences settings =
                                    getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(UploadCallDetails.PREF_ACCOUNT_NAME, accountName);
                            editor.apply();
                            mCredential.setSelectedAccountName(accountName);
                        }
                        new RetrieveExchangeCodeAsyncTask(this).execute();
                        new RetrieveJwtAsyncTask(this).execute();
                    }
                }
                break;
            // user has returned back from the permissions screen,
            // if he/she has given enough permissions, retry the the request.
            case UploadCallDetails.REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    // replay the same operations
                    new RetrieveExchangeCodeAsyncTask(this).execute();
                    new RetrieveJwtAsyncTask(this).execute();
                }
                break;
            case UploadCallDetails.COMPLETE_AUTHORIZATION_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    DialerLogs.messageI(TAG, "Google App is authorized, you can go back to sending the API request");
                    try {
                        UploadCallDetails.postDataFromApi((UploadModel) data.getSerializableExtra(CallExtras.GOOGLE_DATA), "Notifications");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // App is authorized, you can go back to sending the API request
                } else {
                    DialerLogs.messageI(TAG, "Google User denied access, show him the account chooser again");

                    // User denied access, show him the account chooser again
                }
                break;
        }


        if (requestCode == Helper.CROP_ACTIVITY) {
            if (data != null && data.hasExtra(Helper.IMAGE_PATH)) {
                Uri imagePath = Uri.parse(data.getStringExtra(Helper.IMAGE_PATH));
                if (imagePath != null) {
                    preferenceEndPoint.saveStringPreference(Constants.IMAGE_PATH, imagePath.getPath());
                    uploadFile(new File(imagePath.getPath()));
                }
            }
        } else {
            if (getFragment() != null) {
                getFragment().onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void uploadFile(final File file) {
        if (!mHelper.isConnected()) {
            mToastFactory.showToast(getResources().getString(R.string.connectivity_network_settings));
            return;
        }

        showProgressDialog();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("user[avatar]", file.getName(), requestFile);
        String access = "Bearer " + preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.updateProfile(userId, access, null, null, null, null, null, null, null, null, body).enqueue(new Callback<UserProfileInfo>() {
            @Override
            public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                dismissProgressDialog();
                if (response.body() != null) {
                    preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
                }
                if (getFragment() != null && getFragment() instanceof MoreFragment) {
                    ((MoreFragment) getFragment()).loadImage();
                }

            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                dismissProgressDialog();
            }
        });
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
            if (menu != null) {
                menu.clear();
            }
            menu = ((ChatFragment) getFragment()).getMenu();
        } else if (getFragment() instanceof NewContactsFragment) {
            menu = ((NewContactsFragment) getFragment()).getMenu();
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
                    preferenceEndPoint.saveBooleanPreference(Constants.USER_TYPE, response.body().isRepresentative());

                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                //Log.e(TAG, t.getMessage());
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
                   Log.i(TAG, "FCM token updated successfully");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i(TAG, "FCM token failure : " + t.getMessage());
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
                            /*DecimalFormat df = new DecimalFormat("0.000");
                            String format = df.format(Double.valueOf(balanceHelper.getCurrentBalance()));*/
                            preferenceEndPoint.saveStringPreference(Constants.CURRENT_BALANCE, balanceHelper.getCurrentBalance());
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

        Intent intent = new Intent(getApplicationContext(), FirebaseService.class);
        startService(intent);

        if (preferenceEndPoint.getIntPreference(Constants.NOTIFICATION_COUNT) == 0) {
            notificationCount.setVisibility(View.GONE);
        } else if (preferenceEndPoint.getIntPreference(Constants.NOTIFICATION_COUNT) > 0) {
            update(preferenceEndPoint.getIntPreference(Constants.NOTIFICATION_COUNT));
        }
    }

    private void startServiceToFetchNewArticles() {
        // Start service using AlarmManager
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Intent intent = new Intent(getAppContext(), FetchNewArticlesService.class);
        pintent = PendingIntent.getService(this, 1014, intent,
                0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                Constants.FETCHING_NEW_ARTICLES_FREQUENCY, pintent);
    }

    public static Context getAppContext() {
        return BottomTabsActivity.mContext;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sipBinder != null) {
            unbindService(connection);
        }
        EventBus.getDefault().unregister(this);
    }
}
