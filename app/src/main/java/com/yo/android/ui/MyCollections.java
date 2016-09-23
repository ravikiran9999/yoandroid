package com.yo.android.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.MyCollectionsAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.Collections;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCollections extends BaseActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    MyCollectionsAdapter myCollectionsAdapter;
    private boolean contextualMenu;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_magazine);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "My Collections";

        getSupportActionBar().setTitle(title);

        gridView = (GridView) findViewById(R.id.create_magazines_gridview);

        myCollectionsAdapter = new MyCollectionsAdapter(MyCollections.this);
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
                myCollectionsAdapter.addItems(collectionsList);
                gridView.setAdapter(myCollectionsAdapter);

            }

            @Override
            public void onFailure(Call<List<Collections>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
        gridView.setOnItemLongClickListener(this);
        gridView.setOnItemClickListener(this);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
            contextualMenu = true;
            invalidateOptionsMenu();
            myCollectionsAdapter.setContextualMenuEnable(contextualMenu);
            myCollectionsAdapter.getItem(position).setSelect(true);
            myCollectionsAdapter.notifyDataSetChanged();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (dismissContextualMenu()) {
            invalidateOptionsMenu();
            List<Collections> collections = myCollectionsAdapter.getSelectedItems();
            collections.clear();
            myCollectionsAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }

    }

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
                myCollectionsAdapter.notifyDataSetChanged();
                //
                if (myCollectionsAdapter.getSelectedItems().size() == 0) {
                    dismissContextualMenu();
                    invalidateOptionsMenu();
                }
            }

        } else {
            invalidateOptionsMenu();
            if (position == 0 && "Follow more topics".equalsIgnoreCase(collections.getName())) {
                Intent intent = new Intent(MyCollections.this, FollowMoreTopicsActivity.class);
                intent.putExtra("From", "MyCollections");
                startActivityForResult(intent, 2);
            } else {
                Intent intent = new Intent(MyCollections.this, MyCollectionDetails.class);
                intent.putExtra("TopicId", collections.getId());
                intent.putExtra("TopicName", collections.getName());
                intent.putExtra("Type", collections.getType());
                startActivityForResult(intent, 6);
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

        Util.prepareSearch(this, menu, myCollectionsAdapter);
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

    private void showDeleteAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.delete_topic_message));
        builder.setCancelable(false);

        builder.setPositiveButton(
                getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        dialog.dismiss();
                        deleteTopic();
                    }
                });

        builder.setNegativeButton(
                getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteTopic() {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        List<Collections> collections = myCollectionsAdapter.getSelectedItems();
        List<String> topicIds = new ArrayList<String>();
        List<String> magazineIds = new ArrayList<>();
        for (int i = 0; i < collections.size(); i++) {
            if("Tag".equals(collections.get(i).getType())) {
                topicIds.add(collections.get(i).getId());
            } else {
                magazineIds.add(collections.get(i).getId());
            }
        }
        if(topicIds.size() > 0) {
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
                            for(int i=0; i<collectionsList.size(); i++) {
                               followedTopicsIdsList.add(collectionsList.get(i).getId());
                            }
                            preferenceEndPoint.saveStringPreference("magazine_tags", TextUtils.join(",", followedTopicsIdsList));

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

        if(magazineIds.size() > 0) {
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
                            for(int i=0; i<collectionsList.size(); i++) {
                                followedTopicsIdsList.add(collectionsList.get(i).getId());
                            }
                            preferenceEndPoint.saveStringPreference("magazine_tags", TextUtils.join(",", followedTopicsIdsList));

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
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.getCollectionsAPI(accessToken).enqueue(new Callback<List<Collections>>() {
                @Override
                public void onResponse(Call<List<Collections>> call, Response<List<Collections>> response) {
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
                    // do nothing
                }
            });

        } else if (requestCode == 6) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.getCollectionsAPI(accessToken).enqueue(new Callback<List<Collections>>() {
                @Override
                public void onResponse(Call<List<Collections>> call, Response<List<Collections>> response) {
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
                 // do nothing
                }
            });
        }
    }
}
