package com.yo.android.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.FindPeopleAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.FindPeople;
import com.yo.android.util.Util;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowersActivity extends BaseActivity {

    @Inject
    YoApi.YoService yoService;
    private ListView lvFindPeople;
    private FindPeopleAdapter findPeopleAdapter;
    private TextView noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Followers";

        getSupportActionBar().setTitle(title);

        findPeopleAdapter = new FindPeopleAdapter(this);
        lvFindPeople = (ListView) findViewById(R.id.lv_find_people);
        noData = (TextView) findViewById(R.id.no_data);
        lvFindPeople.setAdapter(findPeopleAdapter);

        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getFollowersAPI(accessToken).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body().size() > 0) {
                    noData.setVisibility(View.GONE);
                    lvFindPeople.setVisibility(View.VISIBLE);
                    List<FindPeople> findPeopleList = response.body();
                    findPeopleAdapter.addItems(findPeopleList);
                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        Util.prepareSearch(this, menu, findPeopleAdapter);
        return super.onCreateOptionsMenu(menu);
    }
}
