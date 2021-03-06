package com.yo.android.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.FindPeopleAdapter;
import com.yo.android.adapters.MagazinesTabHeaderAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.FindPeople;
import com.yo.android.util.Constants;
import com.yo.android.util.YODialogs;
import com.yo.android.widgets.ScrollTabHolder;
import com.yo.android.util.Util;
import com.yo.dialer.DialerLogs;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used to display all the Yo App users
 */
public class FindPeopleActivity extends BaseActivity implements AdapterView.OnItemClickListener {


    /*@Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.viewpager)
    protected ViewPager viewPager;*/
    @Bind(R.id.lv_find_people)
    ListView lvFindPeople;
    @Bind(R.id.imv_empty_followings)
    ImageView imvEmptyFindPeople;
    @Bind(R.id.no_data)
    TextView noData;
    @Bind(R.id.ll_no_people)
    LinearLayout llNoPeople;
    @Bind(R.id.network_failure)
    TextView networkFailureText;

    /*@Bind(R.id.tablayout)
    TabLayout tabLayout;*/
    /*@Bind(R.id.header)
    LinearLayout headerLayout;*/
    /*@Bind(R.id.collapse_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;*/

    @Inject
    YoApi.YoService yoService;

    private FindPeopleAdapter findPeopleAdapter;
    private int pageCount = 1;
    private List<FindPeople> originalList;
    private Menu menu1;
    private int pos;
    private SearchView searchView;
    private Call<List<FindPeople>> call;
    public int mMinHeaderTranslation;
    public int mHeaderHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);
        ButterKnife.bind(this);

        initToolbar();
        // Todo uncomment while implementing tabs
        /*setupTabPager();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.htab_tabs);
        tabLayout.setupWithViewPager(viewPager);
        collapsingToolbarLayout.setTitleEnabled(false);
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_full_height);
        mMinHeaderTranslation = -mHeaderHeight + getResources().getDimensionPixelSize(R.dimen.tablayout_height);*/


        findPeopleAdapter = new FindPeopleAdapter(this);
        lvFindPeople.setAdapter(findPeopleAdapter);
        lvFindPeople.setOnScrollListener(onScrollListener());
        imvEmptyFindPeople.setImageResource(R.drawable.ic_empty_find_people);
        originalList = new ArrayList<>();
        //swipeRefreshContainer.setOnRefreshListener(this);
        lvFindPeople.setOnItemClickListener(this);

        String regId = preferenceEndPoint.getStringPreference(Constants.FCM_REFRESH_TOKEN);
        DialerLogs.messageI("FindPeopleActivity", "The fcm token is " + regId);
    }

    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.find_people);
        }
    }

    private void setupTabPager() {
        String[] titles = getResources().getStringArray(R.array.yo_people_tabs_titles);
        MagazinesTabHeaderAdapter viewPagerAdapter =
                new MagazinesTabHeaderAdapter(getSupportFragmentManager(), titles);
        //viewPager.addOnPageChangeListener(this);
        //viewPager.setAdapter(viewPagerAdapter);
        //viewPager.setOffscreenPageLimit(2);
        //tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchView != null) {
            if (searchView.isIconified() || TextUtils.isEmpty(searchView.getQuery())) {
                pageCount = 1;
                callFindPeopleService(null);
            } else {
                callSearchingService(searchView.getQuery().toString());
            }
        } else {
            callFindPeopleService(null);
        }
    }

    /**
     * Getting the Yo app users
     */
    private void callFindPeopleService(final SwipeRefreshLayout swipeRefreshContainer) {
        if (swipeRefreshContainer != null) {
            swipeRefreshContainer.setRefreshing(false);
        } else {
            showProgressDialog();
        }
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getFindPeopleAPI(accessToken, 1, 30).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                try {
                    if (swipeRefreshContainer != null) {
                        swipeRefreshContainer.setRefreshing(false);
                    } else {
                        dismissProgressDialog();
                    }
                    if (response.body() != null && response.body().size() > 0) {
                        List<FindPeople> findPeopleList = response.body();
                        findPeopleAdapter.clearAll();
                        findPeopleAdapter.addItemsAll(findPeopleList);

                        // Todo comment below lines
                        lvFindPeople.setVisibility(View.VISIBLE);
                        noData.setVisibility(View.GONE);
                        llNoPeople.setVisibility(View.GONE);
                        originalList = response.body();
                        networkFailureText.setVisibility(View.GONE);

                    } else {
                        // Todo comment below lines
                        noData.setVisibility(View.GONE);
                        llNoPeople.setVisibility(View.VISIBLE);
                        lvFindPeople.setVisibility(View.GONE);
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
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                //Todo uncomment
                /*if (swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }*/
                // Todo comment below lines
                noData.setVisibility(View.GONE);
                llNoPeople.setVisibility(View.GONE);
                lvFindPeople.setVisibility(View.GONE);
                networkFailureText.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        menu1 = menu;
        searchPeople(menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean isMoreLoading = false;

    private AbsListView.OnScrollListener onScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int threshold = 1;
                int count = lvFindPeople.getCount();
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (isMoreLoading == false && lvFindPeople.getLastVisiblePosition() >= count - threshold && searchView.isIconified() || TextUtils.isEmpty(searchView.getQuery())) {
                        doPagination();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                // do nothing
            }
        };
    }

    /**
     * Used to implement pagination
     */
    private void doPagination() {
        isMoreLoading = true;
        pageCount++;
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getFindPeopleAPI(accessToken, pageCount, 30).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, @Nonnull Response<List<FindPeople>> response) {
                try {
                    dismissProgressDialog();
                    if (response.body() != null && response.body().size() > 0) {
                        List<FindPeople> findPeopleList = response.body();
                        findPeopleAdapter.addItemsAll(findPeopleList);
                        originalList.addAll(findPeopleList);
                    }
                    isMoreLoading = false;
                } finally {
                    if (response != null && response.body() != null) {
                        try {
                            response.body().clear();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
                isMoreLoading = false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 8 && resultCode == RESULT_OK) {
            if (data != null) {
                if ("Following".equals(data.getStringExtra("FollowState"))) {
                    findPeopleAdapter.getItem(pos).setIsFollowing("true");
                    if (!hasDestroyed()) {
                        findPeopleAdapter.notifyDataSetChanged();
                    }
                } else {
                    findPeopleAdapter.getItem(pos).setIsFollowing("false");
                    if (!hasDestroyed()) {
                        findPeopleAdapter.notifyDataSetChanged();
                    }
                }
            }

        }
    }

    /**
     * Searches in the list of Yo app users
     *
     * @param menu
     */
    private void searchPeople(Menu menu) {
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

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
                if (newText.length() >= 3) {
                    callSearchingService(newText);
                }
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Util.hideKeyboard(FindPeopleActivity.this, FindPeopleActivity.this.getCurrentFocus());
                findPeopleAdapter.clearAll();
                findPeopleAdapter.addItemsAll(originalList);

                lvFindPeople.setVisibility(View.VISIBLE);
                noData.setVisibility(View.GONE);
                llNoPeople.setVisibility(View.GONE);
                networkFailureText.setVisibility(View.GONE);
                return true;
            }
        });
    }

    /**
     * Calls the service to get the list of Yo app users with the search text
     *
     * @param newText The search text
     */
    private void callSearchingService(String newText) {

        String searchKey = newText.trim();
        if (searchKey.isEmpty()) {
            findPeopleAdapter.clearAll();
            findPeopleAdapter.addItemsAll(originalList);
            lvFindPeople.setVisibility(View.VISIBLE);
            noData.setVisibility(View.GONE);
            llNoPeople.setVisibility(View.GONE);
            networkFailureText.setVisibility(View.GONE);
        } else {
            showProgressDialog();
            mProgressDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            if (call != null) {
                call.cancel();
            }
            call = yoService.searchInFindPeople(accessToken, searchKey, 1, 100);
            call.enqueue(new Callback<List<FindPeople>>() {
                @Override
                public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                    dismissProgressDialog();
                    if (response.body() != null && response.body().size() > 0) {
                        try {
                            List<FindPeople> findPeopleList = response.body();
                            findPeopleAdapter.clearAll();
                            findPeopleAdapter.addItemsAll(findPeopleList);

                            // Todo comment below lines
                            lvFindPeople.setVisibility(View.VISIBLE);
                            noData.setVisibility(View.GONE);
                            llNoPeople.setVisibility(View.GONE);
                            networkFailureText.setVisibility(View.GONE);
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

                    } else {
                        // Todo comment below lines
                        noData.setVisibility(View.VISIBLE);
                        llNoPeople.setVisibility(View.VISIBLE);
                        lvFindPeople.setVisibility(View.GONE);
                        networkFailureText.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                    dismissProgressDialog();
                    // Todo comment below lines
                    noData.setVisibility(View.GONE);
                    llNoPeople.setVisibility(View.GONE);
                    lvFindPeople.setVisibility(View.GONE);
                    networkFailureText.setVisibility(View.VISIBLE);
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

    /**
     * Refreshes the list of Yo app users
     */
    public void refresh() {
        callFindPeopleService(null);
        pageCount = 1;
        findPeopleAdapter.clearAll();
        findPeopleAdapter.addItemsAll(originalList);
        lvFindPeople.setVisibility(View.VISIBLE);
        if (originalList.size() > 0) {
            noData.setVisibility(View.GONE);
            llNoPeople.setVisibility(View.GONE);
            networkFailureText.setVisibility(View.GONE);
        }
    }

    //@Override
    public void onRefresh() {
        //callFindPeopleService(swipeRefreshContainer);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean renewalStatus = preferenceEndPoint.getBooleanPreference(Constants.MAGAZINE_LOCK, false);
        if (!renewalStatus) {
            pos = position;
            if (findPeopleAdapter.getCount() > position) {
                FindPeople item = findPeopleAdapter.getItem(position);
                if (item != null) {
                    Intent otherProfileIntent = new Intent(FindPeopleActivity.this, OthersProfileActivity.class);
                    otherProfileIntent.putExtra(Constants.USER_ID, item.getId());
                    otherProfileIntent.putExtra("PersonName", item.getFirst_name() + " " + item.getLast_name());
                    otherProfileIntent.putExtra("PersonPic", item.getAvatar());
                    otherProfileIntent.putExtra("PersonIsFollowing", item.getIsFollowing());
                    otherProfileIntent.putExtra("MagazinesCount", item.getMagzinesCount());
                    otherProfileIntent.putExtra("FollowersCount", item.getFollowersCount());
                    otherProfileIntent.putExtra("LikedArticlesCount", item.getLikedArticlesCount());
                    startActivityForResult(otherProfileIntent, 8);
                }
            }
        } else {
            YODialogs.renewMagazine(this, null, R.string.renewal_message, preferenceEndPoint);
        }
    }
}
