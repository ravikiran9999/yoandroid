package com.yo.android.chat.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.adapters.CountryCodeListAdapter;
import com.yo.android.model.CountryCode;
import com.yo.android.util.Constants;
import com.yo.android.util.CountryCodeHelper;
import com.yo.android.R;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CountryCodeActivity extends ParentActivity {

    @Inject
    CountryCodeHelper mCountryCodeHelper;

    @Bind(R.id.countrylist_item)
    ListView countryList;
    @Bind(R.id.no_search_results)
    TextView noSearchResultsTxtVw;

    private List<CountryCode> mList;
    private MenuItem searchMenuItem;
    private  CountryCodeListAdapter countryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_code);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.activity_title_yourcountry);


        List<String> list = new ArrayList<>();
        mList = mCountryCodeHelper.readCodesFromAssets();
        countryAdapter = new CountryCodeListAdapter(this);
        countryAdapter.addItems(mList);
        countryList.setAdapter(countryAdapter);

        countryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                Intent intent = getIntent();

                intent.putExtra(Constants.COUNTRY_CODE, countryAdapter.getItem(i).getCountryCode());
                intent.putExtra("COUNTRY_ID", countryAdapter.getItem(i).getCountryID());
                intent.putExtra("COUNTRY_NAME", countryAdapter.getItem(i).getCountryName());
                setResult(RESULT_OK, intent);

                finish();

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();


        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        Util.prepareSearch(this, menu,countryAdapter, noSearchResultsTxtVw,countryList,null, null);
        return super.onCreateOptionsMenu(menu);
    }

}