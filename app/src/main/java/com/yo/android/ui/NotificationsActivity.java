package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.NotificationsAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.notification.helper.NotificationCache;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.model.Articles;
import com.yo.android.model.FindPeople;
import com.yo.android.model.Notification;
import com.yo.android.ui.fragments.InviteActivity;
import com.yo.android.util.Constants;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends BaseActivity {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private ListView lvNotifications;
    private NotificationsAdapter notificationsAdapter;
    private TextView noData;
    private LinearLayout llNoNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Notifications";

        getSupportActionBar().setTitle(title);

        EventBus.getDefault().register(this);
        preferenceEndPoint.saveIntPreference(Constants.NOTIFICATION_COUNT, 0);
        preferenceEndPoint.saveBooleanPreference("isNotifications", true);

        notificationsAdapter = new NotificationsAdapter(this);
        lvNotifications = (ListView) findViewById(R.id.lv_notifications);
        noData = (TextView) findViewById(R.id.no_data);
        llNoNotifications = (LinearLayout) findViewById(R.id.ll_no_notifications);
        lvNotifications.setAdapter(notificationsAdapter);

        NotificationCache.clearNotifications();

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        showProgressDialog();
        yoService.getNotifications(accessToken).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                dismissProgressDialog();
                if (response == null || response.body() == null || response.body().isEmpty()) {
                    lvNotifications.setVisibility(View.GONE);
                    noData.setVisibility(View.GONE);
                    llNoNotifications.setVisibility(View.VISIBLE);
                    return;
                }
                if (response != null && response.body().size() > 0) {
                    List<Notification> notificationList = response.body();
                    notificationsAdapter.addItems(notificationList);
                    lvNotifications.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);
                    llNoNotifications.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                dismissProgressDialog();
                lvNotifications.setVisibility(View.GONE);
                noData.setVisibility(View.GONE);
                llNoNotifications.setVisibility(View.VISIBLE);
            }
        });

        lvNotifications.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String redirectId = notificationsAdapter.getItem(position).getId();
                String tag = notificationsAdapter.getItem(position).getTag();
                String title = notificationsAdapter.getItem(position).getTitle();

                if("User".equals(tag)) {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.getUserInfoFromId(redirectId, accessToken).enqueue(new Callback<FindPeople>() {
                        @Override
                        public void onResponse(Call<FindPeople> call, Response<FindPeople> response) {

                            if (response.body() != null) {
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
                            }
                        }

                        @Override
                        public void onFailure(Call<FindPeople> call, Throwable t) {

                        }
                    });

                } else if("Topic".equals(tag)) {
                    Intent intent = new Intent(NotificationsActivity.this, MyCollectionDetails.class);
                    intent.putExtra("TopicId", redirectId);
                    intent.putExtra("TopicName", title);
                    intent.putExtra("Type", "Tag");
                    startActivity(intent);
                } else if("Article".equals(tag)) {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.getArticleInfo(redirectId, accessToken).enqueue(new Callback<Articles>() {
                        @Override
                        public void onResponse(Call<Articles> call, Response<Articles> response) {
                            if (response.body() != null) {
                                Articles articles = response.body();
                                Intent intent = new Intent(NotificationsActivity.this, MagazineArticleDetailsActivity.class);
                                intent.putExtra("Title", articles.getTitle());
                                intent.putExtra("Image", articles.getUrl());
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onFailure(Call<Articles> call, Throwable t) {

                        }
                    });

                } else if("Magzine".equals(tag)) {
                    Intent intent = new Intent(NotificationsActivity.this, MyCollectionDetails.class);
                    intent.putExtra("TopicId", redirectId);
                    intent.putExtra("TopicName", title);
                    intent.putExtra("Type", "Magzine");
                    startActivity(intent);
                } else if("Recharge".equals(tag) || "Credit".equals(tag) || "BalanceTransferred".equals(tag)) {
                    startActivity(new Intent(NotificationsActivity.this, TabsHeaderActivity.class));
                } else if("Broadcast".equals(tag) || "Tip".equals(tag) || "PriceUpdate".equals(tag)) {
                    if (redirectId.equals("AddFriends")) {
                        startActivity(new Intent(NotificationsActivity.this, InviteActivity.class));
                    } else if (redirectId.equals("AddBalance")) {
                        startActivity(new Intent(NotificationsActivity.this, TabsHeaderActivity.class));
                    }

                }
            }
        });
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
            yoService.getNotifications(accessToken).enqueue(new Callback<List<Notification>>() {
                @Override
                public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                    dismissProgressDialog();
                    if (response == null || response.body() == null) {
                        lvNotifications.setVisibility(View.GONE);
                        noData.setVisibility(View.GONE);
                        llNoNotifications.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (response != null && response.body().size() > 0) {
                        List<Notification> notificationList = response.body();
                        notificationsAdapter.addItems(notificationList);
                        lvNotifications.setVisibility(View.VISIBLE);
                        noData.setVisibility(View.GONE);
                        llNoNotifications.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onFailure(Call<List<Notification>> call, Throwable t) {
                    dismissProgressDialog();
                    lvNotifications.setVisibility(View.GONE);
                    noData.setVisibility(View.GONE);
                    llNoNotifications.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
