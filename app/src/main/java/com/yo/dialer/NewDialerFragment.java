package com.yo.dialer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.adapters.CallLogsAdapter;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.dialer.CallLogsResult;
import com.yo.android.model.dialer.OpponentDetails;
import com.yo.android.ui.NewDailerActivity;
import com.yo.android.ui.fragments.DialerFragment;
import com.yo.android.util.Constants;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;
import com.yo.android.util.YODialogs;
import com.yo.dialer.model.CallLog;
import com.yo.services.BackgroundServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by Rajesh Babu on 11/7/17.
 */

public class NewDialerFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener {

    public static final String REFRESH_CALL_LOGS = "com.yo.android.ACTION_REFRESH_CALL_LOGS";

    private static final String TAG = NewDialerFragment.class.getSimpleName();

    private EventBus bus = EventBus.getDefault();

    //Required to use DialerFragment context when even required.
    private Activity activity;

    //To handle navigation menu options across the application.
    private Menu menu;

    //Show call history
    private CallLogsAdapter adapter;

    //Handle previous search result when move back and forth.
    private SearchView searchView;

    // To handle PopUps in Dialer
    private boolean isAlreadyShownABoolean;
    private boolean isSharedPreferenceShown;

    // Used to show no call logs message on the view.
    @BindView(R.id.ll_no_calls)
    LinearLayout llNoCalls;

    //While searching if there are no results show message using this view.
    @BindView(R.id.no_search_results)
    protected TextView noSearchResult;

    @BindView(R.id.listView)
    ListView callLogListView;

    @BindView(R.id.txtEmptyCallLogs)
    TextView txtEmptyCallLogs;

    //Used to read contact object based on nexge username.
    @Inject
    ContactsSyncManager mContactsSyncManager;

    public static final int REFRESH_CALL_LOGS_TIME = 1000;

    private ArrayList<Map.Entry<String, List<CallLogsResult>>> appCalls = new ArrayList<Map.Entry<String, List<CallLogsResult>>>();
    private ArrayList<Map.Entry<String, List<CallLogsResult>>> paidCalls = new ArrayList<Map.Entry<String, List<CallLogsResult>>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_dialer_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        registerListeners();

        //For fetching call rates.

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
        callLogListView.setAdapter(adapter);
        //Open more options in call log.
        callLogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.showView(position);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        this.activity = getActivity();
        startBackgroundService();
        if (searchView != null && !TextUtils.isEmpty(searchView.getQuery())) {
            searchView.setQuery(searchView.getQuery(), false);
        } else {
            readCallLogs();
            refreshCallLogsIfNotUpdated();
        }
    }

    private void readCallLogs() {
        appCalls.clear();
        paidCalls.clear();
        final String filter = preferenceEndPoint.getStringPreference(Constants.DIALER_FILTER, Filter.ALL_CALLS);
        FilterData filterData = Filter.getFilterType(filter, activity, appCalls, paidCalls);
        CallLogs.load(activity, new CallLogCompleteLister() {
            @Override
            public void callLogsCompleted(CallLog callLog) {
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appCalls = com.yo.android.calllogs.CallLog.Calls.getAppToAppCallLog(activity);
                            paidCalls = com.yo.android.calllogs.CallLog.Calls.getPSTNCallLog(activity);
                            showEmptyText();
                            showFilteredCallLogs();
                        }
                    });
                }
            }
        }, filterData.getFilterType());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @OnClick(R.id.floatingDialer)
    public void onDialerClick() {
        //TODO: If no call rates fetched dont navigate to dialer screen.
        startActivity(new Intent(getActivity(), NewDailerActivity.class));
    }

    private void registerListeners() {
        setHasOptionsMenu(true);
        bus.register(this);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_dialer, menu);
        this.menu = menu;
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        Util.changeSearchProperties(menu);


        //Handle search start and close events.
        handleSearchViewActions();

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //initializing for search
        Util.prepareContactsSearch(getActivity(), menu, adapter, Constants.DAILER_FRAG, noSearchResult, null);

        int itemId = item.getItemId();
        if (itemId == R.id.menu_clear_history) {
            showClearCallHistoryDialog();
        } else if (itemId == android.R.id.home) {
            showEmptyText();
        } else {
            Filter.save(itemId, preferenceEndPoint);
            showFilteredCallLogs();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilteredCallLogs() {
        Filter.filteredData(activity, preferenceEndPoint, appCalls, paidCalls, new CallLogCompleteLister() {

            @Override
            public void callLogsCompleted(final CallLog callLog) {
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clearAll();
                            adapter.addItemsAll(callLog.getResults());
                            showEmptyText();
                        }
                    });
                }

            }
        });

    }

    private void handleSearchViewActions() {
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                //When expand,user start the search so hide no call log messages if visible.
                //TODO: if its visible mean no logs then no need to hide?
                llNoCalls.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                showEmptyText();
                noSearchResult.setVisibility(View.GONE);
                if (activity != null) {
                    activity.invalidateOptionsMenu();
                }
                readCallLogs();
                return true;
            }
        });
    }

    private void showClearCallHistoryDialog() {
        YODialogs.clearHistory(activity, new DialerFragment.CallLogClearListener() {
            @Override
            public void clear() {
                clearCallLogs();
            }
        });
    }

    private void clearCallLogs() {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CallLogs.load(activity, null, CallLogs.CLEAR_CALL_LOGS);
                    appCalls.clear();
                    paidCalls.clear();
                    adapter.clearAll();
                    showEmptyText();
                }
            });
        }
    }

    /**
     * Background service to fetch call rates
     */
    private void startBackgroundService() {
        Intent intent = new Intent(activity, BackgroundServices.class);
        intent.setAction(BackgroundServices.FETCH_CALL_RATES);
        activity.startService(intent);
    }

    /**
     * Return menu object to handle dialer menu from BottomTabactivity(Launcher activity)
     *
     * @return menu object
     */
    public Menu getMenu() {
        return menu;
    }

    /**
     * @param action
     */
    public void onEventMainThread(Object action) {
        if (DialerConfig.ENABLE_LOGS) {
            Log.w(TAG, "LOADING CALL LOGS AFTER ACTION " + action);
        }
        if (action instanceof String) {
            if (action.equals(REFRESH_CALL_LOGS)) {
                if (action != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            readCallLogs();
                            refreshCallLogsIfNotUpdated();
                            mBalanceHelper.checkBalance(null);
                        }
                    });
                }
            } /*else if (action.equals(Constants.BALANCE_RECHARGE_ACTION)) {
                Dialogs.recharge(activity);
            }*/
        } /*else if (action instanceof OpponentDetails) {
            DialerLogs.messageI(TAG, "Service not available or user not found so PSTN dialog");
            OpponentDetails opponentDetails = (OpponentDetails) action;
            if (opponentDetails != null && opponentDetails.getContact().getNexgieUserName() != null && opponentDetails.getContact().getNexgieUserName().contains(BuildConfig.RELEASE_USER_TYPE)) {
                if (opponentDetails.getStatusCode() == CallExtras.StatusCode.YO_INV_STATE_CALLEE_NOT_ONLINE) {
                    YODialogs.redirectToPSTN(bus, getActivity(), opponentDetails, preferenceEndPoint, mBalanceHelper, mToastFactory);
                }
            } else if (opponentDetails != null && opponentDetails.getVoxUserName() != null) {
                //This case is phone number is not save in his device bu the callee is voxuser
                opponentDetails.getContact().setNexgieUserName(opponentDetails.getVoxUserName());


                if (opponentDetails.getStatusCode() == CallExtras.StatusCode.YO_INV_STATE_CALLEE_NOT_ONLINE) {
                    YODialogs.redirectToPSTN(bus, getActivity(), opponentDetails, preferenceEndPoint, mBalanceHelper, mToastFactory);
                }
            }
        }*/
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if (preferenceEndPoint != null) {
                // Capture user id
                Map<String, String> dialerParams = new HashMap<String, String>();
                String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                //param keys and values have to be of String type
                dialerParams.put("UserId", userId);

                FlurryAgent.logEvent("Dialer", dialerParams, true);
            }
        }
        if (DialerConfig.SHOW_POPUPS) {
            DialerPopUp.showPopUp(this, isVisibleToUser, preferenceEndPoint, isAlreadyShownABoolean, isSharedPreferenceShown);
        }
    }

    @Override
    public void closePopup() {
        if (DialerConfig.SHOW_POPUPS) {
            DialerPopUp.closePopup(preferenceEndPoint, isSharedPreferenceShown);
        }
    }

    private void showEmptyText() {
        try {
            final String filter = preferenceEndPoint.getStringPreference(Constants.DIALER_FILTER, Filter.ALL_CALLS);
            if (filter.equalsIgnoreCase(Filter.ALL_CALLS)) {
                txtEmptyCallLogs.setVisibility(View.GONE);
                txtEmptyCallLogs.setText(R.string.no_call_history);
                llNoCalls.setVisibility(View.VISIBLE);
            } else {
                txtEmptyCallLogs.setVisibility(View.GONE);
                txtEmptyCallLogs.setText(String.format(getResources().getString(R.string.no_history), filter));
                llNoCalls.setVisibility(View.VISIBLE);
            }
            noSearchResult.setVisibility(View.GONE);
            boolean nonEmpty = (callLogListView.getAdapter() != null && callLogListView.getAdapter().getCount() > 0);
            txtEmptyCallLogs.setVisibility(View.GONE);
            llNoCalls.setVisibility(nonEmpty ? View.GONE : View.VISIBLE);
            callLogListView.setVisibility(nonEmpty ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            mLog.w(TAG, e);
        }
    }

    private void refreshCallLogsIfNotUpdated() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                readCallLogs();
            }
        }, REFRESH_CALL_LOGS_TIME);
    }
}
