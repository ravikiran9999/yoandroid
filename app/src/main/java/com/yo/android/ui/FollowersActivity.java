package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used to display the list of the user's followers
 */
public class FollowersActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.lv_find_people)
    protected ListView lvFindPeople;
    @Bind(R.id.no_search_results)
    protected TextView noData;
    @Bind(R.id.ll_no_people)
    protected LinearLayout llNoPeople;
    @Bind(R.id.imv_empty_followings)
    protected ImageView imvEmptyFollowers;
    @Bind(R.id.network_failure)
    protected TextView networkFailureText;
    /*@Bind(R.id.swipeContainer)
    protected SwipeRefreshLayout swipeRefreshContainer;*/

    public boolean isNetworkFailure;
    private FindPeopleAdapter findPeopleAdapter;
    public boolean isEmptyDataSet;

    @Inject
    YoApi.YoService yoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);
        ButterKnife.bind(this);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getString(R.string.followers);

        getSupportActionBar().setTitle(title);

        findPeopleAdapter = new FindPeopleAdapter(this);
        imvEmptyFollowers.setImageResource(R.drawable.ic_empty_followers);
        lvFindPeople.setAdapter(findPeopleAdapter);
        //swipeRefreshContainer.setOnRefreshListener(this);
        followers(null);
        lvFindPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent otherProfileIntent = new Intent(FollowersActivity.this, OthersProfileActivity.class);
                otherProfileIntent.putExtra(Constants.USER_ID, findPeopleAdapter.getItem(position).getId());
                otherProfileIntent.putExtra("PersonName", findPeopleAdapter.getItem(position).getFirst_name() + " " + findPeopleAdapter.getItem(position).getLast_name());
                otherProfileIntent.putExtra("PersonPic", findPeopleAdapter.getItem(position).getAvatar());
                otherProfileIntent.putExtra("PersonIsFollowing", findPeopleAdapter.getItem(position).getIsFollowing());
                otherProfileIntent.putExtra("MagazinesCount", findPeopleAdapter.getItem(position).getMagzinesCount());
                otherProfileIntent.putExtra("FollowersCount", findPeopleAdapter.getItem(position).getFollowersCount());
                otherProfileIntent.putExtra("LikedArticlesCount", findPeopleAdapter.getItem(position).getLikedArticlesCount());
                startActivityForResult(otherProfileIntent, 9);
            }
        });

    }

    private void followers(final SwipeRefreshLayout swipeRefreshContainer) {
        if(swipeRefreshContainer != null) {
            swipeRefreshContainer.setRefreshing(false);
        } else {
            showProgressDialog();
        }
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getFollowersAPI(accessToken).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                if (response.body() != null && response.body().size() > 0) {
                    noData.setVisibility(View.GONE);
                    llNoPeople.setVisibility(View.GONE);
                    lvFindPeople.setVisibility(View.VISIBLE);
                    networkFailureText.setVisibility(View.GONE);
                    List<FindPeople> findPeopleList = response.body();
                    findPeopleAdapter.addItems(findPeopleList);
                    isEmptyDataSet = false;
                    isNetworkFailure = false;
                } else {
                    noData.setVisibility(View.GONE);
                    llNoPeople.setVisibility(View.VISIBLE);
                    lvFindPeople.setVisibility(View.GONE);
                    networkFailureText.setVisibility(View.GONE);
                    isEmptyDataSet = true;
                    isNetworkFailure = false;
                }
                if(swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }

            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                if(swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }
                noData.setVisibility(View.GONE);
                llNoPeople.setVisibility(View.GONE);
                lvFindPeople.setVisibility(View.GONE);
                networkFailureText.setVisibility(View.VISIBLE);
                isEmptyDataSet = false;
                isNetworkFailure = true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        Util.preparePeopleSearch(this, menu, findPeopleAdapter, noData, lvFindPeople, null, llNoPeople, networkFailureText);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 9 && resultCode == RESULT_OK) {
            if (data != null) {
                showProgressDialog();
                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                yoService.getFollowersAPI(accessToken).enqueue(new Callback<List<FindPeople>>() {
                    @Override
                    public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                        if (response!=null && response.body()!=null && response.body().size() > 0) {
                            noData.setVisibility(View.GONE);
                            llNoPeople.setVisibility(View.GONE);
                            lvFindPeople.setVisibility(View.VISIBLE);
                            networkFailureText.setVisibility(View.GONE);
                            List<FindPeople> findPeopleList = response.body();
                            findPeopleAdapter.clearAll();
                            findPeopleAdapter.addItems(findPeopleList);
                            isEmptyDataSet = false;
                            isNetworkFailure = false;
                        } else {
                            noData.setVisibility(View.GONE);
                            llNoPeople.setVisibility(View.VISIBLE);
                            lvFindPeople.setVisibility(View.GONE);
                            networkFailureText.setVisibility(View.GONE);
                            isEmptyDataSet = true;
                            isNetworkFailure = false;
                        }
                        dismissProgressDialog();
                    }

                    @Override
                    public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                        dismissProgressDialog();
                        noData.setVisibility(View.GONE);
                        llNoPeople.setVisibility(View.GONE);
                        lvFindPeople.setVisibility(View.GONE);
                        networkFailureText.setVisibility(View.VISIBLE);
                        isEmptyDataSet = false;
                        isNetworkFailure = true;
                    }
                });
            }

        }
    }

    @Override
    public void onRefresh() {
        //followers(swipeRefreshContainer);
    }
}
