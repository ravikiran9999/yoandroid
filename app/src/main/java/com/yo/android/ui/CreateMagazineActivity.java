package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactoryImpl;
import com.yo.android.R;
import com.yo.android.adapters.CreateMagazinesAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.OwnMagazine;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateMagazineActivity extends BaseActivity {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private GridView gridView;
    private List<OwnMagazine> ownMagazineList;
    private String addArticleMagazineId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_magazine);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Create Magazine";

        getSupportActionBar().setTitle(title);


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.MAGAZINE_ADD_ARTICLE_ID)) {
            addArticleMagazineId = intent.getStringExtra(Constants.MAGAZINE_ADD_ARTICLE_ID);
        }

        gridView = (GridView) findViewById(R.id.create_magazines_gridview);

        final String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getMagazinesAPI(accessToken).enqueue(new Callback<List<OwnMagazine>>() {
            @Override
            public void onResponse(Call<List<OwnMagazine>> call, Response<List<OwnMagazine>> response) {

                ownMagazineList = new ArrayList<OwnMagazine>();
                OwnMagazine ownMagazine = new OwnMagazine();
                ownMagazine.setName("+ New Magazine");
                ownMagazine.setImage("");
                ownMagazineList.add(ownMagazine);

                if (response == null || response.body() == null) {
                    return;
                }
                for (int i = 0; i < response.body().size(); i++) {
                    ownMagazineList.add(response.body().get(i));
                }

                CreateMagazinesAdapter createMagazinesAdapter = new CreateMagazinesAdapter(CreateMagazineActivity.this, ownMagazineList);
                gridView.setAdapter(createMagazinesAdapter);
            }

            @Override
            public void onFailure(Call<List<OwnMagazine>> call, Throwable t) {

            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Intent intent = new Intent(CreateMagazineActivity.this, NewMagazineActivity.class);
                    startActivityForResult(intent, 2);// Activity is started with requestCode 2
                } else if (addArticleMagazineId != null) {
                    List<String> articlesList = new ArrayList<>();
                    articlesList.add(addArticleMagazineId);

                    yoService.addArticleMagazineApi(accessToken, ownMagazineList.get(position).getId(), articlesList).enqueue(new Callback<com.yo.android.model.Response>() {
                        @Override
                        public void onResponse(Call<com.yo.android.model.Response> call, Response<com.yo.android.model.Response> response) {
                            if (response.code() == Constants.SUCCESS_CODE) {
                                setResult(RESULT_OK, new Intent());
                                addArticleMagazineId = null;
                                finish();
                            } else {
                                new ToastFactoryImpl(CreateMagazineActivity.this).showToast(getResources().getString(R.string.some_thing_wrong));
                            }
                        }

                        @Override
                        public void onFailure(Call<com.yo.android.model.Response> call, Throwable t) {
                            new ToastFactoryImpl(CreateMagazineActivity.this).showToast(getResources().getString(R.string.some_thing_wrong));
                        }
                    });
                } else {
                    Intent intent = new Intent(CreateMagazineActivity.this, CreatedMagazineDetailActivity.class);
                    intent.putExtra("MagazineTitle", ownMagazineList.get(position).getName());
                    intent.putExtra("MagazineId", ownMagazineList.get(position).getId());
                    intent.putExtra("MagazineDesc", ownMagazineList.get(position).getDescription());
                    intent.putExtra("MagazinePrivacy", ownMagazineList.get(position).getPrivacy());
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2) {
            ownMagazineList.clear();
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.getMagazinesAPI(accessToken).enqueue(new Callback<List<OwnMagazine>>() {
                @Override
                public void onResponse(Call<List<OwnMagazine>> call, Response<List<OwnMagazine>> response) {

                    //List<OwnMagazine> ownMagazineList = new ArrayList<OwnMagazine>();
                    OwnMagazine ownMagazine = new OwnMagazine();
                    ownMagazine.setName("+ New Magazine");
                    ownMagazine.setImage("");
                    ownMagazineList.add(ownMagazine);

                    if (response == null || response.body() == null) {
                        return;
                    }
                    for (int i = 0; i < response.body().size(); i++) {
                        ownMagazineList.add(response.body().get(i));
                    }

                    CreateMagazinesAdapter createMagazinesAdapter = new CreateMagazinesAdapter(CreateMagazineActivity.this, ownMagazineList);
                    gridView.setAdapter(createMagazinesAdapter);
                }

                @Override
                public void onFailure(Call<List<OwnMagazine>> call, Throwable t) {

                }
            });
        }
    }
}
