package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.NotificationsAdapter;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.chat.notification.helper.NotificationCache;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.model.Articles;
import com.yo.android.model.FindPeople;
import com.yo.android.model.Notification;
import com.yo.android.ui.fragments.InviteActivity;
import com.yo.android.usecase.NotificationUsecase;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used to display the list of Notifications
 */
public class NotificationsActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String CLEAR = "clear";

    @Bind(R.id.lv_notifications)
    protected ListView lvNotifications;
    @Bind(R.id.no_data)
    protected TextView noData;
    @Bind(R.id.ll_no_notifications)
    protected LinearLayout llNoNotifications;
    @Bind(R.id.network_failure)
    protected TextView networkFailureText;
    @Bind(R.id.swipeContainer)
    protected SwipeRefreshLayout swipeRefreshContainer;

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private NotificationsAdapter notificationsAdapter;

    @Inject
    NotificationUsecase notificationUsecase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        ButterKnife.bind(this);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Notifications";
        getSupportActionBar().setTitle(title);
        EventBus.getDefault().register(this);

        preferenceEndPoint.saveBooleanPreference("isNotifications", true);

        notificationsAdapter = new NotificationsAdapter(this);
        lvNotifications.setAdapter(notificationsAdapter);

        getNotifications(null);
        swipeRefreshContainer.setOnRefreshListener(this);
        lvNotifications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String redirectId = notificationsAdapter.getItem(position).getId();
                String tag = notificationsAdapter.getItem(position).getTag();
                String title = notificationsAdapter.getItem(position).getTitle();
                lvNotifications.setEnabled(false);

                if ("User".equals(tag)) {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.getUserInfoFromId(redirectId, accessToken).enqueue(new Callback<FindPeople>() {
                        @Override
                        public void onResponse(Call<FindPeople> call, Response<FindPeople> response) {

                            if (response.body() != null) {
                                try {
                                    FindPeople userInfo = response.body();
                                    Intent intent = new Intent(NotificationsActivity.this, OthersProfileActivity.class);
                                    intent.putExtra(Constants.USER_ID, redirectId);
                                    intent.putExtra("PersonName", userInfo.getFirst_name() + " " + userInfo.getLast_name());
                                    intent.putExtra("PersonPic", userInfo.getAvatar());
                                    intent.putExtra("PersonIsFollowing", userInfo.getIsFollowing());
                                    intent.putExtra("MagazinesCount", userInfo.getMagzinesCount());
                                    intent.putExtra("FollowersCount", userInfo.getFollowersCount());
                                    intent.putExtra("LikedArticlesCount", userInfo.getLikedArticlesCount());
                                    startActivity(intent);
                                } finally {
                                    if(response != null && response.body() != null) {
                                        try {
                                            response = null;
                                        }catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<FindPeople> call, Throwable t) {
                            lvNotifications.setEnabled(true);
                        }
                    });

                } else if ("Topic".equals(tag)) {
                    Intent intent = new Intent(NotificationsActivity.this, MyCollectionDetails.class);
                    intent.putExtra("TopicId", redirectId);
                    intent.putExtra("TopicName", title);
                    intent.putExtra("Type", "Tag");
                    startActivity(intent);
                } else if ("Article".equals(tag)) {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.getArticleInfo(redirectId, accessToken).enqueue(new Callback<Articles>() {
                        @Override
                        public void onResponse(Call<Articles> call, Response<Articles> response) {
                            if (response.body() != null) {
                                try {
                                    Articles articles = response.body();
                                    Intent intent = new Intent(NotificationsActivity.this, MagazineArticleDetailsActivity.class);
                                    intent.putExtra("Title", articles.getTitle());
                                    intent.putExtra("Image", articles.getUrl());
                                    intent.putExtra("Article", articles);
                                    intent.putExtra("Position", 0);
                                    startActivity(intent);
                                } finally {
                                    if(response != null && response.body() != null) {
                                        try {
                                            response = null;
                                        }catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Articles> call, Throwable t) {
                            lvNotifications.setEnabled(true);
                        }
                    });

                } else if ("Magzine".equals(tag)) {
                    Intent intent = new Intent(NotificationsActivity.this, MyCollectionDetails.class);
                    intent.putExtra("TopicId", redirectId);
                    intent.putExtra("TopicName", title);
                    intent.putExtra("Type", "Magzine");
                    startActivity(intent);
                } else if ("Recharge".equals(tag) || "Credit".equals(tag) || "BalanceTransferred".equals(tag)) {
                    startActivity(new Intent(NotificationsActivity.this, TabsHeaderActivity.class));
                } else if ("Broadcast".equals(tag) || "Tip".equals(tag) || "PriceUpdate".equals(tag)) {
                    if (redirectId.equals("AddFriends")) {
                        startActivity(new Intent(NotificationsActivity.this, InviteActivity.class));
                    } else if (redirectId.equals("AddBalance")) {
                        startActivity(new Intent(NotificationsActivity.this, TabsHeaderActivity.class));
                    } else {
                        lvNotifications.setEnabled(true);
                    }

                }
            }
        });
    }

    private void getNotifications(final SwipeRefreshLayout swipeRefreshContainer) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        if (swipeRefreshContainer != null) {
            swipeRefreshContainer.setRefreshing(false);
        } else {
            showProgressDialog();
        }
        yoService.getNotifications(accessToken, "", "").enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }
                try {
                    if (response == null || response.body() == null || response.body().isEmpty()) {
                        lvNotifications.setVisibility(View.GONE);
                        noData.setVisibility(View.GONE);
                        llNoNotifications.setVisibility(View.VISIBLE);
                        networkFailureText.setVisibility(View.GONE);
                        return;
                    }
                    if (response != null && response.body().size() > 0) {
                        preferenceEndPoint.saveIntPreference(Constants.NOTIFICATION_COUNT, 0);
                        NotificationCache.clearNotifications();
                        List<Notification> notificationList = response.body();
                        notificationsAdapter.addItems(notificationList);
                        lvNotifications.setVisibility(View.VISIBLE);
                        noData.setVisibility(View.GONE);
                        llNoNotifications.setVisibility(View.GONE);
                        networkFailureText.setVisibility(View.GONE);
                    }
                } finally {
                    if (response != null && response.body() != null) {
                        try {
                            response.body().clear();
                            response = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                if (swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }
                lvNotifications.setVisibility(View.GONE);
                noData.setVisibility(View.GONE);
                llNoNotifications.setVisibility(View.GONE);
                networkFailureText.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear) {
            List<String> clearNotifications = new ArrayList<>();
            for (Notification notification : notificationsAdapter.getAllItems()) {
                clearNotifications.add(notification.getNotification_id());
            }
            if (clearNotifications.size() > 0) {
                String notificationIds = new Gson().toJson(clearNotifications);
                notificationUsecase.getNotifications(notificationIds, CLEAR, new ApiCallback<List<Notification>>() {
                    @Override
                    public void onResult(List<Notification> result) {
                        notificationsAdapter.getAllItems().clear();

                        lvNotifications.setVisibility(View.GONE);
                        llNoNotifications.setVisibility(View.VISIBLE);

                        notificationsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(String message) {

                    }
                });

            }
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.saveBooleanPreference("isNotifications", false);
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(final String action) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onEventMainThread(action);
            }
        });
    }

    public void onEventMainThread(String action) {
        if (Constants.UPDATE_NOTIFICATIONS.equals(action)) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            showProgressDialog();
            yoService.getNotifications(accessToken, "", "").enqueue(new Callback<List<Notification>>() {
                @Override
                public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                    dismissProgressDialog();
                    try {
                        if (response == null || response.body() == null) {
                            lvNotifications.setVisibility(View.GONE);
                            noData.setVisibility(View.GONE);
                            llNoNotifications.setVisibility(View.VISIBLE);
                            networkFailureText.setVisibility(View.GONE);
                            return;
                        }
                        if (response != null && response.body().size() > 0) {
                            NotificationCache.clearNotifications();
                            preferenceEndPoint.saveIntPreference(Constants.NOTIFICATION_COUNT, 0);
                            List<Notification> notificationList = response.body();
                            notificationsAdapter.addItems(notificationList);
                            lvNotifications.setVisibility(View.VISIBLE);
                            noData.setVisibility(View.GONE);
                            llNoNotifications.setVisibility(View.GONE);
                            networkFailureText.setVisibility(View.GONE);
                        }
                    }finally {
                        if(response != null && response.body() != null) {
                            try {
                                response.body().clear();
                                response = null;
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Notification>> call, Throwable t) {
                    dismissProgressDialog();
                    lvNotifications.setVisibility(View.GONE);
                    noData.setVisibility(View.GONE);
                    llNoNotifications.setVisibility(View.GONE);
                    networkFailureText.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        lvNotifications.setEnabled(true);
    }

    @Override
    public void onRefresh() {
        getNotifications(swipeRefreshContainer);
    }
}
