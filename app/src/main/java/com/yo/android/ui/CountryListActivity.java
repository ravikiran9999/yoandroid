package com.yo.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yo.android.R;
import com.yo.android.adapters.AbstractBaseAdapter;
import com.yo.android.helpers.CallRatesCountryViewHolder;
import com.yo.android.model.dialer.CallRateDetail;
import com.yo.android.model.dialer.CallRatesResponse;
import com.yo.android.util.Constants;

import java.io.InputStreamReader;
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
    @Bind(R.id.txtEmpty)
    TextView txtEmptyView;
    CountyCallRatesAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_yo_contacts);
        ButterKnife.bind(this);
        enableBack();
        getSupportActionBar().setTitle("Select Country");
        adapter = new CountyCallRatesAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        showProgressDialog();
        voxService.executeAction(voxFactory.getCallRatesBody("1")).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dismissProgressDialog();
                try {
                    CallRatesResponse response1 = new Gson().fromJson(new InputStreamReader(response.body().byteStream()), CallRatesResponse.class);
                    List<CallRateDetail> callRateDetailList = response1.getData().getCallRateDetailList();
                    adapter.addItems(callRateDetailList);
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
        txtEmptyView.setText("No country list available.");
        txtEmptyView.setVisibility(adapter.getCount() == 0 ? View.VISIBLE : View.GONE);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = parent.getAdapter().getItem(position);
        if (object instanceof CallRateDetail) {
            CallRateDetail callRateDetail = (CallRateDetail) object;
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CALL_RATE, callRateDetail.getRate());
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_NAME, callRateDetail.getDestination());
            preferenceEndPoint.saveStringPreference(Constants.COUNTRY_CALL_PULSE, callRateDetail.getPulse());
            setResult(RESULT_OK);
            finish();
        }
    }

    public class CountyCallRatesAdapter extends AbstractBaseAdapter<CallRateDetail, CallRatesCountryViewHolder> {

        public CountyCallRatesAdapter(Context context) {
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
        public void bindView(int position, CallRatesCountryViewHolder holder, CallRateDetail item) {
            holder.getCountryView().setText(item.getDestination() + " (+" + item.getPrefix() + ")");
            String pulse;
            if (item.getPulse().equals("60")) {
                pulse = "min";
            } else {
                pulse = "sec";
            }
            holder.getCallRateView().setText("$ " + item.getRate() + "/" + pulse);
        }
    }
}
