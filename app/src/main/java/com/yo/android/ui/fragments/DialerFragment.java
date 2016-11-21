package com.yo.android.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.CallLogsAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.calllogs.CallLog;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.Popup;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.model.dialer.CallRateDetail;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.NewDailerActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;
import com.yo.android.util.YODialogs;
import com.yo.android.vox.BalanceHelper;
import com.yo.android.vox.VoxFactory;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 3/7/16.
 */
public class DialerFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener {

    public static final String REFRESH_CALL_LOGS = "com.yo.android.ACTION_REFRESH_CALL_LOGS";

    @Inject
    VoxFactory voxFactory;
    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.progress)
    ProgressBar progress;
    @Bind(R.id.txtEmptyCallLogs)
    TextView txtEmptyCallLogs;
    @Bind(R.id.floatingDialer)
    View floatingDialer;
    @Bind(R.id.ll_no_calls)
    LinearLayout llNoCalls;

    private MenuItem searchMenuItem;
    private SearchView searchView;
    private static final String TAG = "DialerFragment";
    private EventBus bus = EventBus.getDefault();
    private CallLogsAdapter adapter;

    //private ImageView btnDialer;

    private View bottom_layout;

    private boolean show;
    @Inject
    ConnectivityHelper mConnectivityHelper;
    @Inject
    BalanceHelper mBalanceHelper;
    @Inject
    @Named("voip_support")
    boolean isVoipSupported;
    private Menu menu;
    private ArrayList<Map.Entry<String, List<CallLogsResult>>> appCalls = new ArrayList<Map.Entry<String, List<CallLogsResult>>>();
    private ArrayList<Map.Entry<String, List<CallLogsResult>>> paidCalls = new ArrayList<Map.Entry<String, List<CallLogsResult>>>();
    private ArrayList<Map.Entry<String, List<CallLogsResult>>> results = new ArrayList<>();
    @Inject
    protected YoApi.YoService yoService;

    @Inject
    ContactsSyncManager mContactsSyncManager;

    public boolean isFromDailer = false;
    private boolean isAlreadyShown;
    private boolean isRemoved;

    public interface CallLogClearListener {
        public void clear();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_dialer_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        bus.register(this);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        yoService.getCallsRatesListAPI(preferenceEndPoint.getStringPreference("access_token")).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dismissProgressDialog();
                try {
                    List<CallRateDetail> callRateDetailList = new Gson().fromJson(new InputStreamReader(response.body().byteStream()), new TypeToken<List<CallRateDetail>>() {
                    }.getType());
                    if (callRateDetailList != null && !callRateDetailList.isEmpty()) {
                        String json = new Gson().toJson(callRateDetailList);
                        preferenceEndPoint.saveStringPreference(Constants.COUNTRY_LIST, json);
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_dialer, menu);
        this.menu = menu;
        Util.prepareContactsSearch(getActivity(), menu, adapter, Constants.DAILER_FRAG);
        Util.changeSearchProperties(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // hideDialPad(true);
        String str = null;
        if (item.getItemId() == R.id.menu_all_calls) {
            str = "all calls";
        } else if (item.getItemId() == R.id.menu_paid_calls) {
            str = "paid calls";
        } else if (item.getItemId() == R.id.menu_app_calls) {
            str = "app calls";
        } else if (item.getItemId() == R.id.menu_clear_history) {
            YODialogs.clearHistory(getActivity(), new CallLogClearListener() {
                @Override
                public void clear() {
                    clearCallLogs();
                }
            });
        }

        if (str != null) {
            preferenceEndPoint.saveStringPreference(Constants.DIALER_FILTER, str);
            showDataOnFilter();
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearCallLogs() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CallLog.Calls.clearCallHistory(getActivity());
                adapter.clearAll();
                showEmptyText();
            }
        });

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new CallLogsAdapter(getActivity(), preferenceEndPoint, mContactsSyncManager);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(showCallLogDetailsListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCallLogs();
    }

    AdapterView.OnItemClickListener showCallLogDetailsListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            adapter.showView(position);
        }
    };

    @OnClick(R.id.floatingDialer)
    public void onDialerClick() {
        startActivity(new Intent(getActivity(), NewDailerActivity.class));
        //  getActivity().overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        // showDialPad();
    }

    public void loadCallLogs() {
        appCalls.clear();
        paidCalls.clear();
        appCalls = CallLog.Calls.getAppToAppCallLog(getActivity());
        paidCalls = CallLog.Calls.getPSTNCallLog(getActivity());
        showEmptyText();
        showDataOnFilter();
    }

    private void showDataOnFilter() {
        final String filter = preferenceEndPoint.getStringPreference(Constants.DIALER_FILTER, "all calls");
        ArrayList<Map.Entry<String, List<CallLogsResult>>> tempResults = new ArrayList<>();
        results.clear();
        if (filter.equalsIgnoreCase("all calls")) {
            results = prepare("All Calls", results, CallLog.Calls.getCallLog(getActivity()));
        } else if (filter.equalsIgnoreCase("app calls")) {
            results = prepare("Free Calls", results, appCalls);
        } else {
            results = prepare("Paid Calls", results, paidCalls);
        }
        tempResults.addAll(results);
        results.clear();
        adapter.clearAll();
        results.addAll(tempResults);
        adapter.addItemsAll(results);
        tempResults = null;
        showEmptyText();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isFromDailer = true;
    }

    private ArrayList<Map.Entry<String, List<CallLogsResult>>> prepare(String type, ArrayList<Map.Entry<String, List<CallLogsResult>>> results, ArrayList<Map.Entry<String, List<CallLogsResult>>> checkList) {
        if (!checkList.isEmpty()) {
            List<CallLogsResult> resultList = new ArrayList<>();
            HashMap<String, List<CallLogsResult>> hashMap = new HashMap<>();
            CallLogsResult result = new CallLogsResult();
            result.setHeader(true);
            result.setHeaderTitle(type);
            resultList.add(result);
            hashMap.put(type, resultList);
            results = new ArrayList(hashMap.entrySet());
            results.addAll(checkList);
        }
        return results;
    }


    private void showEmptyText() {
        try {
            final String filter = preferenceEndPoint.getStringPreference(Constants.DIALER_FILTER, "all calls");
            if (filter.equalsIgnoreCase("all calls")) {
                txtEmptyCallLogs.setVisibility(View.GONE);
                txtEmptyCallLogs.setText("No call logs history available.");
                llNoCalls.setVisibility(View.VISIBLE);
            } else {
                txtEmptyCallLogs.setVisibility(View.GONE);
                txtEmptyCallLogs.setText(String.format("No %s history available.", filter));
                llNoCalls.setVisibility(View.VISIBLE);
            }
            boolean nonEmpty = show || (listView.getAdapter() != null && listView.getAdapter().getCount() > 0);
            txtEmptyCallLogs.setVisibility(View.GONE);
            llNoCalls.setVisibility(nonEmpty ? View.GONE : View.VISIBLE);
            listView.setVisibility(nonEmpty ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    @Override
    public void showOrHideTabs(boolean show) {
        super.showOrHideTabs(show);
    }

    @Override
    public void showProgressDialog() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgressDialog() {
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
    }

    public Menu getMenu() {
        return menu;
    }


    /**
     * @param action
     */
    public void onEventMainThread(String action) {
        Log.w(TAG, "LOADING CALL LOGS AFTER ACTION "+action);
        if (action.equals(REFRESH_CALL_LOGS)) {
            loadCallLogs();
            mBalanceHelper.checkBalance(null);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() instanceof BottomTabsActivity) {
                BottomTabsActivity activity = (BottomTabsActivity) getActivity();
                if (activity.getFragment() instanceof DialerFragment) {
                    if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                        Type type = new TypeToken<List<Popup>>() {
                        }.getType();
                        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                        if (popup != null && popup.size() > 0 && popup.get(0).getPopupsEnum() == PopupHelper.PopupsEnum.DIALER) {
                            if (!isAlreadyShown) {
                                PopupHelper.getPopup(PopupHelper.PopupsEnum.DIALER, popup, getActivity(), preferenceEndPoint, this, this);
                                isAlreadyShown = true;
                            }
                        }
                    }
                }
            }

        } else {
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getActivity() instanceof BottomTabsActivity) {
            BottomTabsActivity activity = (BottomTabsActivity) getActivity();
            if (activity.getFragment() instanceof DialerFragment) {
                if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                    if (!isRemoved) {
                        Type type = new TypeToken<List<Popup>>() {
                        }.getType();
                        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                        if (popup != null && popup.size() > 0 && popup.get(0).getPopupsEnum() == PopupHelper.PopupsEnum.DIALER) {
                            if (!isAlreadyShown) {
                                PopupHelper.getPopup(PopupHelper.PopupsEnum.DIALER, popup, getActivity(), preferenceEndPoint, this, this);
                                isAlreadyShown = true;
                            }
                        }
                    } /*else {
                        isRemoved = false;
                    }*/
                }
            }
        }

    }

    @Override
    public void closePopup() {
        isAlreadyShown = false;
        isRemoved = true;
        //preferenceEndPoint.removePreference(Constants.POPUP_NOTIFICATION);
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        popup.remove(0);
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }
}
