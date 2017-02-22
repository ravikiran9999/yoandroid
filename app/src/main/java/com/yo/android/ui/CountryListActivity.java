package com.yo.android.ui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.helpers.CallRatesCountryViewHolder;
import com.yo.android.model.dialer.CallRateDetail;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 9/7/16.
 */
public class CountryListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private final static String TAG = "CountryListActivity";
    @Bind(R.id.lv_app_contacts)
    ListView listView;

    @Bind(R.id.lv_app_contacts_recent)
    ListView listViewRecent;

    @Bind(R.id.tv_recent_title)
    TextView recentTextView;

    @Bind(R.id.no_search_results)
    TextView txtEmptyView;

    @Bind(R.id.side_index)
    ListView layout;

    @Bind(R.id.tv_title)
    TextView countryTitle;

    private CountryCallRatesAdapter adapter;
    private RecentCountryCallRatesAdapter recentAdapter;
    private MenuItem searchMenuItem;
    private Context context;
    private Gson gson;
    private List<CallRateDetail> selectedRecentCallRateDetails = new ArrayList<>();
    List<CallRateDetail> recentCallRateDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_county_list);
        ButterKnife.bind(this);
        context = this;
        enableBack();
        getSupportActionBar().setTitle(R.string.activity_contry_selection_title);
        layout.setVisibility(View.GONE);
        adapter = new CountryCallRatesAdapter(this);
        recentAdapter = new RecentCountryCallRatesAdapter(this);
        recentCallRateDetails = new ArrayList<>();
        listView.setAdapter(adapter);
        listViewRecent.setAdapter(recentAdapter);
        listView.setOnItemClickListener(this);
        listViewRecent.setOnItemClickListener(this);
        showProgressDialog();
        gson = new Gson();
        final String accessToken = preferenceEndPoint.getStringPreference("access_token");
        Type type = new TypeToken<List<CallRateDetail>>() {
        }.getType();
        List<CallRateDetail> callRateDetailList = gson.fromJson(preferenceEndPoint.getStringPreference(Constants.COUNTRY_LIST), type);
        if (callRateDetailList != null) {
            adapter.addItems(callRateDetailList);
            Util.setDynamicHeight(listView);
        }
        try {
            List<CallRateDetail> tempRecentCallRateDetails = gson.fromJson(preferenceEndPoint.getStringPreference(Constants.COUNTRY_CODE_SELECTED), type);
            if (recentCallRateDetails != null) {
                recentCallRateDetails.addAll(tempRecentCallRateDetails);
            }

            if (recentCallRateDetails != null && !recentCallRateDetails.isEmpty()) {
                recentTextView.setVisibility(View.VISIBLE);
                listViewRecent.setVisibility(View.VISIBLE);
                recentAdapter.addItems(recentCallRateDetails);
                Util.setDynamicHeight(listViewRecent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        yoService.getCallsRatesListAPI(accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dismissProgressDialog();
                try {
                    List<CallRateDetail> callRateDetailList = gson.fromJson(new InputStreamReader(response.body().byteStream()), new TypeToken<List<CallRateDetail>>() {
                    }.getType());
                    if (callRateDetailList != null && !callRateDetailList.isEmpty()) {
                        String json = gson.toJson(callRateDetailList);
                        preferenceEndPoint.saveStringPreference(Constants.COUNTRY_LIST, json);
                        adapter.addItems(callRateDetailList);
                        Util.setDynamicHeight(listView);
                        Util.setDynamicHeight(listViewRecent);
                    }
                } catch (Exception e) {
                    mLog.w(TAG, e);
                }
                showEmptyText();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgressDialog();
                showEmptyText();
            }
        });
    }

    private void showEmptyText() {
        txtEmptyView.setText(R.string.empty_country_list_message);
        txtEmptyView.setVisibility(adapter.getCount() == 0 ? View.VISIBLE : View.GONE);
        if(txtEmptyView.getVisibility() == View.VISIBLE) {
            countryTitle.setVisibility(View.GONE);
            recentTextView.setVisibility(View.GONE);
        } else {
            countryTitle.setVisibility(View.VISIBLE);
            recentTextView.setVisibility(recentTextView.getVisibility());
            if(recentAdapter.getAllItems().size()>0) {
                recentTextView.setVisibility(View.VISIBLE);
            } else if(recentAdapter.getCount() == 0) {
                recentTextView.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_country_list, menu);
        prepareSearch(menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void prepareSearch(Menu menu) {
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mLog.i(TAG, "onQueryTextChange: " + query);
                Util.hideKeyboard(context, getCurrentFocus());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.performCountryCodeSearch(newText);
                recentAdapter.performCountryCodeSearch(newText);
                showEmptyText();
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Util.hideKeyboard(context, getCurrentFocus());
                if (adapter != null) {
                    adapter.performCountryCodeSearch("");
                    recentAdapter.performCountryCodeSearch("");
                }
                return true;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = parent.getAdapter().getItem(position);
        if (object instanceof CallRateDetail) {
            CallRateDetail callRateDetail = (CallRateDetail) object;
            callRateDetail.setRecentSelected(true);
            if(!recentCallRateDetails.contains(callRateDetail)) {
                recentCallRateDetails.add(callRateDetail);
            }
            String json = new Gson().toJson(recentCallRateDetails);

            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CALL_RATE, Util.removeTrailingZeros(callRateDetail.getRate()));
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_NAME, callRateDetail.getDestination());
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CALL_PULSE, callRateDetail.getPulse());
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CODE_PREFIX, "+" + callRateDetail.getPrefix());
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CODE_SELECTED, json);
            setResult(RESULT_OK);
            finish();
        }
    }

    public class CountryCallRatesAdapter extends AbstractBaseAdapter<CallRateDetail, CallRatesCountryViewHolder> {

        public CountryCallRatesAdapter(Context context) {
            super(context);
        }

        @Override
        public int getLayoutId() {
            return R.layout.call_rate_country_list_row;
        }

        @Override
        public CallRatesCountryViewHolder getViewHolder(View convertView) {
            return new CallRatesCountryViewHolder(convertView);
        }

        @Override
        protected boolean hasData(CallRateDetail event, String key) {
            return containsValue(event.getDestination().toLowerCase(), key) || containsValue(event.getPrefix().toLowerCase(), key) || super.hasData(event, key);
        }

        private boolean containsValue(String str, String key) {
            return str != null && str.toLowerCase().contains(key);
        }

        @Override
        public void bindView(int position, CallRatesCountryViewHolder holder, CallRateDetail item) {
            holder.getCountryView().setText(item.getDestination() + " (+" + item.getPrefix() + ")");
            String pulse;
            String rate = Util.removeTrailingZeros(item.getRate());
            if (item.getPulse().equals("60")) {
                pulse = "min";
            } else {
                pulse = "sec";
            }
            holder.getCallRateView().setText("$ " + rate + "/" + pulse);
        }
    }

    public class RecentCountryCallRatesAdapter extends AbstractBaseAdapter<CallRateDetail, CallRatesCountryViewHolder> {

        public RecentCountryCallRatesAdapter(Context context) {
            super(context);
        }

        @Override
        public int getLayoutId() {
            return R.layout.call_rate_country_list_row;
        }

        @Override
        public CallRatesCountryViewHolder getViewHolder(View convertView) {
            return new CallRatesCountryViewHolder(convertView);
        }

        @Override
        protected boolean hasData(CallRateDetail event, String key) {
            return containsValue(event.getDestination().toLowerCase(), key) || containsValue(event.getPrefix().toLowerCase(), key) || super.hasData(event, key);
        }

        private boolean containsValue(String str, String key) {
            return str != null && str.toLowerCase().contains(key);
        }

        @Override
        public void bindView(int position, CallRatesCountryViewHolder holder, CallRateDetail item) {
            holder.getCountryView().setText(item.getDestination() + " (+" + item.getPrefix() + ")");
            String pulse;
            String rate = Util.removeTrailingZeros(item.getRate());
            if (item.getPulse().equals("60")) {
                pulse = "min";
            } else {
                pulse = "sec";
            }
            holder.getCallRateView().setText("$ " + rate + "/" + pulse);
        }
    }
}
