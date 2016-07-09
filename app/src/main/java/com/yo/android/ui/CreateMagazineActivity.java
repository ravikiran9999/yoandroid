package com.yo.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.yo.android.R;
import com.yo.android.adapters.CreateMagazinesAdapter;
import com.yo.android.model.OwnMagazine;

import java.util.ArrayList;
import java.util.List;

public class CreateMagazineActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_magazine);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Create Magazine";

        getSupportActionBar().setTitle(title);

        GridView gridView = (GridView) findViewById(R.id.create_magazines_gridview);

        List<OwnMagazine> ownMagazineList = new ArrayList<OwnMagazine>();
        OwnMagazine ownMagazine = new OwnMagazine();
        ownMagazine.setTitle("+ New Magazine");
        ownMagazine.setImage("");
        ownMagazineList.add(ownMagazine);

        CreateMagazinesAdapter createMagazinesAdapter = new CreateMagazinesAdapter(this, ownMagazineList);
        gridView.setAdapter(createMagazinesAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    Intent intent = new Intent(CreateMagazineActivity.this, NewMagazineActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
