package com.yo.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.CreateMagazinesAdapter;
import com.yo.android.adapters.MyCollectionsAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.Collections;
import com.yo.android.model.OwnMagazine;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCollections extends BaseActivity {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_magazine);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "My Collections";

        getSupportActionBar().setTitle(title);

        final GridView gridView = (GridView) findViewById(R.id.create_magazines_gridview);

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getCollectionsAPI(accessToken).enqueue(new Callback<List<Collections>>() {
            @Override
            public void onResponse(Call<List<Collections>> call, Response<List<Collections>> response) {

                List<Collections> collectionsList = new ArrayList<Collections>();
                Collections collections = new Collections();
                collections.setName("Follow more topics");
                collections.setImage("");
                collectionsList.add(0, collections);


                if (response == null || response.body() == null) {
                    return;
                }
                for (int i = 0; i < response.body().size(); i++) {
                    collectionsList.add(response.body().get(i));
                }

                MyCollectionsAdapter myCollectionsAdapter = new MyCollectionsAdapter(MyCollections.this, collectionsList);
                gridView.setAdapter(myCollectionsAdapter);

            }

            @Override
            public void onFailure(Call<List<Collections>> call, Throwable t) {

            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position ==0) {
                    Intent intent = new Intent(MyCollections.this, FollowMoreTopics.class);
                    startActivity(intent);
                }
            }
        });
    }
}
