package com.yo.android.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.adapters.MyCollectionsAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.Collections;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.video.InAppVideoActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used to display the followed topics
 */
public class MyCollections extends BaseActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.create_magazines_gridview)
    protected GridView gridView;
    @Bind(R.id.no_search_results)
    protected TextView noSearchFound;
    @Bind(R.id.swipeContainer)
    protected SwipeRefreshLayout swipeRefreshContainer;

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    protected SearchView searchView;
    MyCollectionsAdapter myCollectionsAdapter;
    private boolean contextualMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_magazine);
        ButterKnife.bind(this);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getString(R.string.my_topics);

        getSupportActionBar().setTitle(title);


        myCollectionsAdapter = new MyCollectionsAdapter(MyCollections.this);

        myCollections(null);


        swipeRefreshContainer.setOnRefreshListener(this);
        gridView.setOnItemLongClickListener(this);
        gridView.setOnItemClickListener(this);
    }

    public void myCollections(final SwipeRefreshLayout swipeRefreshContainer) {
        if (swipeRefreshContainer != null) {
            swipeRefreshContainer.setRefreshing(false);
        } else {
            showProgressDialog();
        }
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        //Todo pass renewal status
        yoService.getCollectionsAPI(accessToken).enqueue(new Callback<List<Collections>>() {
            @Override
            public void onResponse(Call<List<Collections>> call, Response<List<Collections>> response) {

                if (swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }
                final List<Collections> collectionsList = new ArrayList<Collections>();
                Collections collections = new Collections();
                collections.setName("Follow more topics");
                collections.setImage("");
                collectionsList.add(0, collections);
                if (response == null || response.body() == null) {
                    return;
                }
                collectionsList.addAll(response.body());
                myCollectionsAdapter.addItems(collectionsList);
                gridView.setAdapter(myCollectionsAdapter);

            }

            @Override
            public void onFailure(Call<List<Collections>> call, Throwable t) {
                if (swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }
            }
        });
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
            contextualMenu = true;
            invalidateOptionsMenu();
            myCollectionsAdapter.setContextualMenuEnable(contextualMenu);
            myCollectionsAdapter.getItem(position).setSelect(true);
            if (!hasDestroyed()) {
                myCollectionsAdapter.notifyDataSetChanged();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (dismissContextualMenu()) {
            invalidateOptionsMenu();
            List<Collections> collections = myCollectionsAdapter.getSelectedItems();
            collections.clear();
            if (!hasDestroyed()) {
                myCollectionsAdapter.notifyDataSetChanged();
            }
        } else {
            super.onBackPressed();
        }

    }

    /**
     * Dismisses the contextual menu
     *
     * @return
     */
    private boolean dismissContextualMenu() {
        if (contextualMenu) {
            contextualMenu = false;
            myCollectionsAdapter.setContextualMenuEnable(false);

            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Collections collections = (Collections) parent.getAdapter().getItem(position);
        if (contextualMenu) {
            if (position != 0) {
                collections.toggleSelection();
                if (!hasDestroyed()) {
                    myCollectionsAdapter.notifyDataSetChanged();
                }
                //
                if (myCollectionsAdapter.getSelectedItems().size() == 0) {
                    dismissContextualMenu();
                    invalidateOptionsMenu();
                }
            }

        } else {
            if (position == 0 && "Follow more topics".equalsIgnoreCase(collections.getName())) {
                Intent intent = new Intent(MyCollections.this, FollowMoreTopicsActivity.class);
                intent.putExtra("From", "MyCollections");
                startActivityForResult(intent, 2);
            } else {
                String videoUrl = collections.getVideo_url();
                if (videoUrl != null && !TextUtils.isEmpty(videoUrl)) {
                    InAppVideoActivity.start(MyCollections.this, videoUrl, collections.getName());
                } else {
                    Intent intent = new Intent(MyCollections.this, MyCollectionDetails.class);
                    intent.putExtra("TopicId", collections.getId());
                    intent.putExtra("TopicName", collections.getName());
                    intent.putExtra("Type", collections.getType());
                    startActivityForResult(intent, 6);
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my_collections, menu);

        if (contextualMenu) {
            //Show delete icon
            menu.findItem(R.id.menu_delete).setVisible(true);
            //Hide search icon
            menu.findItem(R.id.menu_search).setVisible(false);
        } else {
            menu.findItem(R.id.menu_delete).setVisible(false);
            menu.findItem(R.id.menu_search).setVisible(true);
        }

        Util.prepareSearch(this, menu, myCollectionsAdapter, noSearchFound, null, gridView);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                super.onOptionsItemSelected(item);
                showDeleteAlert();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Shows the confirmation dialog when unfollowing a topic
     */

    private void showDeleteAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.unfollow_alert_dialog, null);
        TextView tvDialogContent = (TextView) view.findViewById(R.id.dialog_content);
        tvDialogContent.setText(getResources().getString(R.string.delete_topic_message));
        builder.setView(view);

        Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
        Button noBtn = (Button) view.findViewById(R.id.no_btn);

        yesBtn.setText(getString(R.string.yes));
        noBtn.setText(getString(R.string.no));

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                deleteTopic();
            }
        });


        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    /**
     * Unfollows the selected topic
     */
    private void deleteTopic() {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        List<Collections> collections = myCollectionsAdapter.getSelectedItems();
        final List<String> topicIds = new ArrayList<String>();
        final List<String> magazineIds = new ArrayList<>();
        for (int i = 0; i < collections.size(); i++) {
            if ("Tag".equals(collections.get(i).getType())) {
                topicIds.add(collections.get(i).getId());
            } else {
                magazineIds.add(collections.get(i).getId());
            }
        }
        if (topicIds.size() > 0) {
            yoService.removeTopicsAPI(accessToken, topicIds).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.getCollectionsAPI(accessToken).enqueue(new Callback<List<Collections>>() {
                        @Override
                        public void onResponse(Call<List<Collections>> call, Response<List<Collections>> response) {
                            dismissContextualMenu();
                            invalidateOptionsMenu();
                            final List<Collections> collectionsList = new ArrayList<Collections>();
                            Collections collections = new Collections();
                            collections.setName("Follow more topics");
                            collections.setImage("");
                            collectionsList.add(0, collections);
                            if (response == null || response.body() == null) {
                                return;
                            }
                            collectionsList.addAll(response.body());
                            myCollectionsAdapter.clearAll();
                            myCollectionsAdapter.addItems(collectionsList);
                            gridView.setAdapter(myCollectionsAdapter);

                            List<String> followedTopicsIdsList = new ArrayList<String>();
                            for (int i = 0; i < collectionsList.size(); i++) {
                                followedTopicsIdsList.add(collectionsList.get(i).getId());
                            }
                            preferenceEndPoint.saveStringPreference("Constants.MAGAZINE_TAGS", TextUtils.join(",", followedTopicsIdsList));

                            if (MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener != null) {
                                for(int i=0; i < topicIds.size(); i++) {
                                    MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener.updateUnfollowTopicStatus(topicIds.get(i), Constants.FOLLOW_TOPIC_EVENT);
                                }
                            }

                        }

                        @Override
                        public void onFailure(Call<List<Collections>> call, Throwable t) {
                            // do nothing
                        }
                    });
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // do nothing
                }
            });
        }

        if (magazineIds.size() > 0) {
            yoService.removeMagazinesAPI(accessToken, magazineIds).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.getCollectionsAPI(accessToken).enqueue(new Callback<List<Collections>>() {
                        @Override
                        public void onResponse(Call<List<Collections>> call, Response<List<Collections>> response) {
                            dismissContextualMenu();
                            invalidateOptionsMenu();
                            final List<Collections> collectionsList = new ArrayList<Collections>();
                            Collections collections = new Collections();
                            collections.setName("Follow more topics");
                            collections.setImage("");
                            collectionsList.add(0, collections);
                            if (response == null || response.body() == null) {
                                return;
                            }
                            collectionsList.addAll(response.body());
                            myCollectionsAdapter.clearAll();
                            myCollectionsAdapter.addItems(collectionsList);
                            gridView.setAdapter(myCollectionsAdapter);

                            List<String> followedTopicsIdsList = new ArrayList<String>();
                            for (int i = 0; i < collectionsList.size(); i++) {
                                followedTopicsIdsList.add(collectionsList.get(i).getId());
                            }
                            preferenceEndPoint.saveStringPreference("Constants.MAGAZINE_TAGS", TextUtils.join(",", followedTopicsIdsList));

                            if (MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener != null) {
                                for(int i=0; i < magazineIds.size(); i++) {
                                    MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener.updateUnfollowTopicStatus(magazineIds.get(i), Constants.FOLLOW_TOPIC_EVENT);
                                }
                            }

                        }

                        @Override
                        public void onFailure(Call<List<Collections>> call, Throwable t) {
                            // do nothing
                        }
                    });
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // do nothing
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            showProgressDialog();
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.getCollectionsAPI(accessToken).enqueue(new Callback<List<Collections>>() {
                @Override
                public void onResponse(Call<List<Collections>> call, Response<List<Collections>> response) {
                    dismissProgressDialog();
                    final List<Collections> collectionsList = new ArrayList<Collections>();
                    Collections collections = new Collections();
                    collections.setName("Follow more topics");
                    collections.setImage("");
                    collectionsList.add(0, collections);
                    if (response == null || response.body() == null) {
                        return;
                    }
                    collectionsList.addAll(response.body());
                    myCollectionsAdapter.clearAll();
                    myCollectionsAdapter.addItems(collectionsList);
                    gridView.setAdapter(myCollectionsAdapter);

                }

                @Override
                public void onFailure(Call<List<Collections>> call, Throwable t) {
                    dismissProgressDialog();
                }
            });

        } else if (requestCode == 6) {
            if (!searchView.isIconified()) {
                invalidateOptionsMenu();
            }
            String searchText = "";
            if (searchView != null) {
                searchText = searchView.getQuery().toString();
            }
            showProgressDialog();
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            final String finalSearchText = searchText;
            yoService.getCollectionsAPI(accessToken).enqueue(new Callback<List<Collections>>() {
                @Override
                public void onResponse(Call<List<Collections>> call, Response<List<Collections>> response) {
                    dismissProgressDialog();
                    final List<Collections> collectionsList = new ArrayList<Collections>();
                    Collections collections = new Collections();
                    collections.setName("Follow more topics");
                    collections.setImage("");
                    collectionsList.add(0, collections);
                    if (response == null || response.body() == null) {
                        return;
                    }

                    /*if (!TextUtils.isEmpty(finalSearchText.trim())) {
                        for (int i = 0; i < response.body().size(); i++) {
                            if (response.body().get(i).getName().contains(finalSearchText)) {
                                collectionsList.addAll(response.body());
                                myCollectionsAdapter.clearAll();
                                myCollectionsAdapter.addItems(collectionsList);
                                break;
                            }
                        }
                    } else {
                        collectionsList.addAll(response.body());
                        myCollectionsAdapter.clearAll();
                        myCollectionsAdapter.addItems(collectionsList);
                    }*/

                    collectionsList.addAll(response.body());
                    myCollectionsAdapter.clearAll();
                    myCollectionsAdapter.addItems(collectionsList);

                    gridView.setAdapter(myCollectionsAdapter);

                    List<String> followedTopicsIdsList = new ArrayList<String>();
                    for (int i = 0; i < collectionsList.size(); i++) {
                        followedTopicsIdsList.add(collectionsList.get(i).getId());
                    }
                    preferenceEndPoint.saveStringPreference(Constants.MAGAZINE_TAGS, TextUtils.join(",", followedTopicsIdsList));

                }

                @Override
                public void onFailure(Call<List<Collections>> call, Throwable t) {
                    dismissProgressDialog();
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        myCollections(swipeRefreshContainer);
    }
}
