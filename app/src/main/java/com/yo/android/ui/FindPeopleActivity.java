package com.yo.android.ui;

import android.os.Bundle;
import android.view.Menu;
import android.widget.AbsListView;
import android.widget.ListView;

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