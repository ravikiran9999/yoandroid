package com.yo.android.chat.ui;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.adapters.CountryCodeListAdapter;
import com.yo.android.helpers.CallRatesCountryViewHolder;
import com.yo.android.helpers.CountryCodeListViewHolder;
import com.yo.android.model.CountryCode;
import com.yo.android.model.dialer.CallRateDetail;
import com.yo.android.util.Constants;
import com.yo.android.util.CountryCodeHelper;
import com.yo.android.R;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class CountryCodeActivity extends ParentActivity {

    @Inject
    CountryCodeHelper mCountryCodeHelper;

    private ListView countryList;
    private List<CountryCode> mList;
    private MenuItem searchMenuItem;
    private  CountryCodeListAdapter countryAdapter;
    private TextView noSearchResultsTxtVw;

    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_code);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.activity_title_yourcountry);


        countryList = (ListView) findViewById(R.id.countrylist_item);
        noSearchResultsTxtVw = (TextView)findViewById(R.id.no_search_results);
        List<String> list = new ArrayList<>();


        mList = mCountryCodeHelper.readCodesFromAssets();
        countryAdapter = new CountryCodeListAdapter(this);
        countryAdapter.addItems(mList);
        countryList.setAdapter(countryAdapter);

        countryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                Intent intent = getIntent();

                intent.putExtra("COUNTRY_CODE", countryAdapter.getItem(i).getCountryCode());
                intent.putExtra("COUNTRY_ID", countryAdapter.getItem(i).getCountryID());
                intent.putExtra("COUNTRY_NAME", countryAdapter.getItem(i).getCountryName());
                // intent.putExtra("COUNTRY_POSITION",i);
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
        Util.prepareSearch(this, menu,countryAdapter, noSearchResultsTxtVw,countryList,null);
        return super.onCreateOptionsMenu(menu);
    }

}








