package com.yo.android.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.NotificationsAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.Notification;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Notifications";

        getSupportActionBar().setTitle(title);

        notificationsAdapter = new NotificationsAdapter(this);
        lvNotifications = (ListView) findViewById(R.id.lv_notifications);
        final TextView noData = (TextView) findViewById(R.id.no_data);
        lvNotifications.setAdapter(notificationsAdapter);

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        showProgressDialog();
        yoService.getNotifications(accessToken).enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                dismissProgressDialog();
                if (response == null || response.body() == null) {
                    lvNotifications.setVisibility(View.GONE);
                    noData.setVisibility(View.VISIBLE);
                    return;
                }
                if (response != null && response.body().size() > 0) {
                    List<Notification> notificationList = response.body();
                    notificationsAdapter.addItemsAll(notificationList);
                    lvNotifications.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                dismissProgressDialog();
                lvNotifications.setVisibility(View.GONE);
                noData.setVisibility(View.VISIBLE);
            }
        });
    }
}
