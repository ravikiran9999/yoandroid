package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactoryImpl;
import com.yo.android.R;
import com.yo.android.adapters.CreateMagazinesAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.OwnMagazine;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used to display the user's created magazines in a GridView
 */
public class CreateMagazineActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {

    @Bind(R.id.swipeContainer)
    protected SwipeRefreshLayout swipeRefreshContainer;
    @Bind(R.id.no_search_results)
    protected TextView noSearchResults;
    @Bind(R.id.create_magazines_gridview)
    protected GridView gridView;

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    CreateMagazinesAdapter createMagazinesAdapter;
    private String addArticleMagazineId = null;
    @Bind(R.id.network_failure)
    protected TextView networkFailureText;
    public boolean isNetworkFailure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_magazine);
        ButterKnife.bind(this);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String title = getString(R.string.my_magazines);

        getSupportActionBar().setTitle(title);

        EventBus.getDefault().register(this);

        if (getIntent() != null && getIntent().hasExtra(Constants.MAGAZINE_ADD_ARTICLE_ID)) {
            addArticleMagazineId = getIntent().getStringExtra(Constants.MAGAZINE_ADD_ARTICLE_ID);
        }

        createMagazinesAdapter = new CreateMagazinesAdapter(CreateMagazineActivity.this);
        gridView.setAdapter(createMagazinesAdapter);
        boolean renewalStatus = preferenceEndPoint.getBooleanPreference(Constants.MAGAZINE_LOCK, false);
        if(!renewalStatus) {
            createMagazine(null);
        }

        swipeRefreshContainer.setOnRefreshListener(this);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                OwnMagazine ownMagazine = (OwnMagazine) parent.getItemAtPosition(position);
                if (position == 0 && "+ New Magazine".equalsIgnoreCase(ownMagazine.getName())) { // The first position and is New Magazine text
                    Intent intent = new Intent(CreateMagazineActivity.this, NewMagazineActivity.class);
                    // Activity is started with requestCode 2
                    startActivityForResult(intent, 2);
                } else if (addArticleMagazineId != null) {
                    List<String> articlesList = new ArrayList<>();
                    articlesList.add(addArticleMagazineId);
                    final String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.addArticleMagazineApi(accessToken, ownMagazine.getId(), articlesList).enqueue(new Callback<com.yo.android.model.Response>() {
                        @Override
                        public void onResponse(Call<com.yo.android.model.Response> call, Response<com.yo.android.model.Response> response) {
                            try {
                                if (response.code() == Constants.SUCCESS_CODE) {
                                    setResult(RESULT_OK, new Intent());
                                    addArticleMagazineId = null;
                                    mToastFactory.showToast("Article added into " + createMagazinesAdapter.getItem(position).getName());
                                    finish();
                                } else {
                                    new ToastFactoryImpl(CreateMagazineActivity.this).showToast("Selected Article already available");
                                    finish();
                                }
                            }finally {
                            }
                        }

                        @Override
                        public void onFailure(Call<com.yo.android.model.Response> call, Throwable t) {
                            new ToastFactoryImpl(CreateMagazineActivity.this).showToast(getResources().getString(R.string.some_thing_wrong));
                        }
                    });
                } else {
                    Intent intent = new Intent(CreateMagazineActivity.this, CreatedMagazineDetailActivity.class);
                    intent.putExtra("MagazineTitle",ownMagazine.getName());
                    intent.putExtra("MagazineId",ownMagazine.getId());
                    intent.putExtra("MagazineDesc", ownMagazine.getDescription());
                    intent.putExtra("MagazinePrivacy", ownMagazine.getPrivacy());
                    startActivityForResult(intent, 2);
                }
            }
        });
    }

    /**
     * Creates the magazine
     * @param swipeRefreshContainer
     */
    private void createMagazine(final SwipeRefreshLayout swipeRefreshContainer) {
        if(swipeRefreshContainer != null) {
            swipeRefreshContainer.setRefreshing(false);
        } else {
            showProgressDialog();
        }
        final String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getMagazinesAPI(accessToken).enqueue(new Callback<List<OwnMagazine>>() {
            @Override
            public void onResponse(Call<List<OwnMagazine>> call, Response<List<OwnMagazine>> response) {
                if(swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }
                networkFailureText.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
                List<OwnMagazine> ownMagazineList;
                ownMagazineList = new ArrayList<OwnMagazine>();
                OwnMagazine ownMagazine = new OwnMagazine();
                ownMagazine.setName("+ New Magazine");
                ownMagazine.setImage("");
                ownMagazineList.add(ownMagazine);
                try {
                    if (response == null || response.body() == null) {
                        return;
                    }
                    ownMagazineList.addAll(response.body());
                    createMagazinesAdapter.addItems(ownMagazineList);
                    isNetworkFailure = false;
                } finally {
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
            public void onFailure(Call<List<OwnMagazine>> call, Throwable t) {
                if(swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }
                networkFailureText.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                isNetworkFailure = true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2) {

            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.getMagazinesAPI(accessToken).enqueue(new Callback<List<OwnMagazine>>() {
                @Override
                public void onResponse(Call<List<OwnMagazine>> call, Response<List<OwnMagazine>> response) {
                    List<OwnMagazine> ownMagazineList = new ArrayList<OwnMagazine>();
                    OwnMagazine ownMagazine = new OwnMagazine();
                    ownMagazine.setName("+ New Magazine");
                    ownMagazine.setImage("");
                    ownMagazineList.add(ownMagazine);
                    try {
                        if (response == null || response.body() == null) {
                            return;
                        }
                        ownMagazineList.addAll(response.body());

                        createMagazinesAdapter.clearAll();
                        createMagazinesAdapter.addItems(ownMagazineList);
                    } finally {
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
                public void onFailure(Call<List<OwnMagazine>> call, Throwable t) {
                  // do nothing
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(String action) {
        if (Constants.DELETE_MAGAZINE_ACTION.equals(action) || Constants.EDIT_MAGAZINE_ACTION.equals(action)) {

            final String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.getMagazinesAPI(accessToken).enqueue(new Callback<List<OwnMagazine>>() {
                @Override
                public void onResponse(Call<List<OwnMagazine>> call, Response<List<OwnMagazine>> response) {
                    List<OwnMagazine> ownMagazineList;
                    ownMagazineList = new ArrayList<OwnMagazine>();
                    OwnMagazine ownMagazine = new OwnMagazine();
                    ownMagazine.setName("+ New Magazine");
                    ownMagazine.setImage("");
                    ownMagazineList.add(ownMagazine);
                    try {
                        if (response == null || response.body() == null) {
                            return;
                        }
                        ownMagazineList.addAll(response.body());
                        createMagazinesAdapter.clearAll();
                        createMagazinesAdapter.addItems(ownMagazineList);
                    } finally {
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
                public void onFailure(Call<List<OwnMagazine>> call, Throwable t) {
                  // do nothing
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        Util.prepareSearch(this, menu, createMagazinesAdapter, noSearchResults, null, gridView, networkFailureText);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRefresh() {
        createMagazine(swipeRefreshContainer);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if(firstVisibleItem >= 2) {
            swipeRefreshContainer.setEnabled(false);
            swipeRefreshContainer.setRefreshing(false);
        } else if(firstVisibleItem == 0) {
            swipeRefreshContainer.setEnabled(true);
        }

    }
}
