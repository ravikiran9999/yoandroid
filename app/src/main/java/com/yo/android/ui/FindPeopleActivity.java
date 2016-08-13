package com.yo.android.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.FindPeopleAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.FindPeople;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
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
    private TextView noData;
    private LinearLayout llNoPeople;
    private List<FindPeople> originalList;
    private Menu menu1;
    private int pos;
    private SearchView searchView;

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
        noData = (TextView) findViewById(R.id.no_data);
        llNoPeople = (LinearLayout) findViewById(R.id.ll_no_people);
        lvFindPeople.setAdapter(findPeopleAdapter);
        lvFindPeople.setOnScrollListener(onScrollListener());
        originalList = new ArrayList<>();

        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getFindPeopleAPI(accessToken, 1, 30).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    List<FindPeople> findPeopleList = response.body();
                    findPeopleAdapter.addItemsAll(findPeopleList);
                    lvFindPeople.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);
                    llNoPeople.setVisibility(View.GONE);
                    originalList = response.body();

                }
                else {
                    noData.setVisibility(View.VISIBLE);
                    llNoPeople.setVisibility(View.VISIBLE);
                    lvFindPeople.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
                noData.setVisibility(View.VISIBLE);
                llNoPeople.setVisibility(View.VISIBLE);
                lvFindPeople.setVisibility(View.GONE);
            }
        });

        lvFindPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos = position;
                Intent otherProfileIntent = new Intent(FindPeopleActivity.this, OthersProfileActivity.class);
                otherProfileIntent.putExtra(Constants.USER_ID, findPeopleAdapter.getItem(position).getId());
                otherProfileIntent.putExtra("PersonName", findPeopleAdapter.getItem(position).getFirst_name() + " " + findPeopleAdapter.getItem(position).getLast_name());
                otherProfileIntent.putExtra("PersonPic", findPeopleAdapter.getItem(position).getAvatar());
                otherProfileIntent.putExtra("PersonIsFollowing", findPeopleAdapter.getItem(position).getIsFollowing());
                otherProfileIntent.putExtra("MagazinesCount", findPeopleAdapter.getItem(position).getMagzinesCount());
                otherProfileIntent.putExtra("FollowersCount", findPeopleAdapter.getItem(position).getFollowersCount());
                otherProfileIntent.putExtra("LikedArticlesCount", findPeopleAdapter.getItem(position).getLikedArticlesCount());
                startActivityForResult(otherProfileIntent, 8);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        menu1 = menu;
        //Util.prepareSearch(this, menu, findPeopleAdapter);
        searchPeople(menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isMoreLoading=false;
    private AbsListView.OnScrollListener onScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int threshold = 1;
                int count = lvFindPeople.getCount();
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (isMoreLoading==false && lvFindPeople.getLastVisiblePosition() >= count - threshold && searchView.isIconified() || TextUtils.isEmpty(searchView.getQuery())) {
                        doPagination();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
            }
        };
    }

    private void doPagination() {
        isMoreLoading=true;
        pageCount++;
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getFindPeopleAPI(accessToken, pageCount, 30).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body().size() > 0) {
                    List<FindPeople> findPeopleList = response.body();
                    findPeopleAdapter.addItemsAll(findPeopleList);
                    originalList.addAll(findPeopleList);
                }
                isMoreLoading=false;
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
                isMoreLoading=false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 8 && resultCode == RESULT_OK) {
            if(data!= null) {
                /*showProgressDialog();
                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                yoService.getFindPeopleAPI(accessToken, 1, 30).enqueue(new Callback<List<FindPeople>>() {
                    @Override
                    public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                        dismissProgressDialog();
                        if (response.body().size() > 0) {
                            List<FindPeople> findPeopleList = response.body();
                            findPeopleAdapter.clearAll();
                            findPeopleAdapter.addItemsAll(findPeopleList);
                            lvFindPeople.setVisibility(View.VISIBLE);
                            noData.setVisibility(View.GONE);
                            llNoPeople.setVisibility(View.GONE);
                            originalList = response.body();

                        }
                        else {
                            noData.setVisibility(View.VISIBLE);
                            llNoPeople.setVisibility(View.VISIBLE);
                            lvFindPeople.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                        dismissProgressDialog();
                        noData.setVisibility(View.VISIBLE);
                        llNoPeople.setVisibility(View.VISIBLE);
                        lvFindPeople.setVisibility(View.GONE);
                    }
                });*/

                if(data.getStringExtra("FollowState").equals("Following")) {
                    findPeopleAdapter.getItem(pos).setIsFollowing("true");
                    findPeopleAdapter.notifyDataSetChanged();
                }
                else {
                    findPeopleAdapter.getItem(pos).setIsFollowing("false");
                    findPeopleAdapter.notifyDataSetChanged();
                }
            }

        }
    }

    private void searchPeople(Menu menu) {
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem;

        searchMenuItem = menu.findItem(R.id.menu_search);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public static final String TAG = "Search in FindPeople";

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "onQueryTextChange: " + query);
                Util.hideKeyboard(FindPeopleActivity.this, FindPeopleActivity.this.getCurrentFocus());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "onQueryTextChange: " + newText);
                /*if (findPeopleAdapter != null) {
                    findPeopleAdapter.performSearch(newText);
                }*/
                callSearchingService(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Util.hideKeyboard(FindPeopleActivity.this, FindPeopleActivity.this.getCurrentFocus());
                /*if (findPeopleAdapter != null) {
                    findPeopleAdapter.performSearch("");
                }*/
                //callSearchingService("");
                findPeopleAdapter.clearAll();
                findPeopleAdapter.addItemsAll(originalList);
                lvFindPeople.setVisibility(View.VISIBLE);
                noData.setVisibility(View.GONE);
                llNoPeople.setVisibility(View.GONE);
                return true;
            }
        });
    }

    private void callSearchingService(String newText) {

        String searchKey = newText.trim();
        if (searchKey.isEmpty()) {
            findPeopleAdapter.clearAll();
            findPeopleAdapter.addItemsAll(originalList);
        }
        else {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.searchInFindPeople(accessToken, newText, 1, 100).enqueue(new Callback<List<FindPeople>>() {
                @Override
                public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                    if (response.body() != null && response.body().size() > 0) {
                        List<FindPeople> findPeopleList = response.body();
                        findPeopleAdapter.clearAll();
                        findPeopleAdapter.addItemsAll(findPeopleList);
                        lvFindPeople.setVisibility(View.VISIBLE);
                        noData.setVisibility(View.GONE);
                        llNoPeople.setVisibility(View.GONE);

                    } else {
                        noData.setVisibility(View.VISIBLE);
                        llNoPeople.setVisibility(View.VISIBLE);
                        lvFindPeople.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                    noData.setVisibility(View.VISIBLE);
                    llNoPeople.setVisibility(View.VISIBLE);
                    lvFindPeople.setVisibility(View.GONE);
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menu1 != null) {
            Util.changeMenuItemsVisibility(menu1, R.id.menu_search, false);
            Util.registerSearchLister(this, menu1);
        }
        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        findPeopleAdapter.clearAll();
        findPeopleAdapter.addItemsAll(originalList);
        lvFindPeople.setVisibility(View.VISIBLE);
        if(originalList.size()> 0) {
            noData.setVisibility(View.GONE);
            llNoPeople.setVisibility(View.GONE);
        }
    }
}
