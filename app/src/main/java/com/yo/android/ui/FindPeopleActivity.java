package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.FindPeopleAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.FindPeople;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindPeopleActivity extends BaseActivity {

    private ListView lvFindPeople;
    private FindPeopleAdapter findPeopleAdapter;
    @Inject
    YoApi.YoService yoService;
    private int pageCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Find People";

        getSupportActionBar().setTitle(title);

        findPeopleAdapter = new FindPeopleAdapter(this);
        lvFindPeople = (ListView) findViewById(R.id.lv_find_people);
        final TextView noData = (TextView) findViewById(R.id.no_data);
        lvFindPeople.setAdapter(findPeopleAdapter);
        lvFindPeople.setOnScrollListener(onScrollListener());

        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getFindPeopleAPI(accessToken, 1, 30).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body().size() > 0) {
                    List<FindPeople> findPeopleList = response.body();
                    findPeopleAdapter.addItemsAll(findPeopleList);
                    lvFindPeople.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
            }
        });

        lvFindPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent otherProfileIntent = new Intent(FindPeopleActivity.this, OthersProfileActivity.class);
                otherProfileIntent.putExtra(Constants.USER_ID, findPeopleAdapter.getItem(position).getId());
                otherProfileIntent.putExtra("PersonName", findPeopleAdapter.getItem(position).getFirst_name() + " " + findPeopleAdapter.getItem(position).getLast_name());
                otherProfileIntent.putExtra("PersonPic", findPeopleAdapter.getItem(position).getAvatar());
                otherProfileIntent.putExtra("PersonIsFollowing", findPeopleAdapter.getItem(position).getIsFollowing());
                startActivity(otherProfileIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        Util.prepareSearch(this, menu, findPeopleAdapter);
        return super.onCreateOptionsMenu(menu);
    }

    private AbsListView.OnScrollListener onScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int threshold = 1;
                int count = lvFindPeople.getCount();
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (lvFindPeople.getLastVisiblePosition() >= count - threshold) {
                        pageCount++;
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.getFindPeopleAPI(accessToken, pageCount, 30).enqueue(new Callback<List<FindPeople>>() {
                            @Override
                            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                                dismissProgressDialog();
                                if (response.body().size() > 0) {
                                    List<FindPeople> findPeopleList = response.body();
                                    findPeopleAdapter.addItemsAll(findPeopleList);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                                dismissProgressDialog();
                            }
                        });
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
            }
        };
    }
}
