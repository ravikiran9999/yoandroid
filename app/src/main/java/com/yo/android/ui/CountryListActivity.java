package com.yo.android.ui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
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

    @Bind(R.id.no_search_results)
    TextView txtEmptyView;

    @Bind(R.id.side_index)
    ListView layout;

    private CountryCallRatesAdapter adapter;
    private MenuItem searchMenuItem;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_yo_contacts);
        ButterKnife.bind(this);
        context = this;
        enableBack();
        getSupportActionBar().setTitle(R.string.activity_contry_selection_title);
        layout.setVisibility(View.GONE);
        adapter = new CountryCallRatesAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        showProgressDialog();
        final Gson gson = new Gson();
        final String accessToken = preferenceEndPoint.getStringPreference("access_token");
        Type type = new TypeToken<List<CallRateDetail>>() {
        }.getType();
        List<CallRateDetail> callRateDetailList = gson.fromJson(preferenceEndPoint.getStringPreference(Constants.COUNTRY_LIST), type);
        if (callRateDetailList != null) {
            adapter.addItems(callRateDetailList);
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
                mLog.i(TAG, "onQueryTextChange: " + newText);
                adapter.performCountryCodeSearch(newText);
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
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CALL_RATE, Util.removeTrailingZeros(callRateDetail.getRate()));
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_NAME, callRateDetail.getDestination());
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CALL_PULSE, callRateDetail.getPulse());
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CODE_PREFIX, "+" + callRateDetail.getPrefix());
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
}
