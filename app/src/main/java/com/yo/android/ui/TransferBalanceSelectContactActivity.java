package com.yo.android.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.TransferBalanceContactAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.helpers.Helper;
import com.yo.android.model.FindPeople;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransferBalanceSelectContactActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private TransferBalanceContactAdapter contactsListAdapter;
    private ListView listView;
    private ListView layout;
    private int pageCount = 1;
    private boolean isRepresentative;
    private String balance;
    private String currencySymbol;
    private Menu menu1;
    private SearchView searchView;
    private List<FindPeople> originalList;
    private Call<List<FindPeople>> call;
    private TextView noData;
    private LinearLayout llNoPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_balance_select_contact);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getString(R.string.select_contact);

        getSupportActionBar().setTitle(title);

        balance = getIntent().getStringExtra("balance");
        currencySymbol = getIntent().getStringExtra("currencySymbol");

        listView = (ListView) findViewById(R.id.lv_contacts);
        layout = (ListView) findViewById(R.id.side_index);
        noData = (TextView) findViewById(R.id.no_data);
        llNoPeople = (LinearLayout) findViewById(R.id.ll_no_people);

        contactsListAdapter = new TransferBalanceContactAdapter(this, preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        listView.setAdapter(contactsListAdapter);
        listView.setOnScrollListener(onScrollListener());
        listView.setOnItemClickListener(this);

        originalList = new ArrayList<>();

        loadUserProfileInfo();
    }

    private void loadUserProfileInfo() {
        String access = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.getUserInfo(access).enqueue(new Callback<UserProfileInfo>() {
            @Override
            public void onResponse(Call<UserProfileInfo> call, Response<UserProfileInfo> response) {
                if (response.body() != null) {
                    Util.saveUserDetails(response, preferenceEndPoint);
                    preferenceEndPoint.saveStringPreference(Constants.USER_ID, response.body().getId());
                    preferenceEndPoint.saveStringPreference(Constants.USER_AVATAR, response.body().getAvatar());
                    preferenceEndPoint.saveStringPreference(Constants.USER_STATUS, response.body().getDescription());
                    preferenceEndPoint.saveStringPreference(Constants.FIREBASE_USER_ID, response.body().getFirebaseUserId());
                    if (TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.USER_NAME))) {
                        preferenceEndPoint.saveStringPreference(Constants.USER_NAME, response.body().getFirstName());
                    }

                    isRepresentative = response.body().isRepresentative();

                    handleRepresentative(isRepresentative);
                    invalidateOptionsMenu();

                } else {
                    noData.setVisibility(View.VISIBLE);
                    llNoPeople.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {
                    noData.setVisibility(View.VISIBLE);
                    llNoPeople.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
            }
        });
    }

    private void handleRepresentative(boolean isRepresentative) {
        if (isRepresentative) {
            callFindPeopleService();
        } else {
            callAppUsersService();
        }
    }

    private void callFindPeopleService() {
        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getRepresentativePeopleAPI(accessToken, 1, 30, true).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    List<FindPeople> findPeopleList = response.body();
                    /*findPeopleAdapter.clearAll();
                    findPeopleAdapter.addItemsAll(findPeopleList);
                    lvFindPeople.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);
                    llNoPeople.setVisibility(View.GONE);*/
                    originalList = response.body();

                    loadAlphabetOrder(findPeopleList);
                    listView.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);
                    llNoPeople.setVisibility(View.GONE);

                } else {
                    noData.setVisibility(View.VISIBLE);
                    llNoPeople.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
                noData.setVisibility(View.VISIBLE);
                llNoPeople.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            }
        });
    }

    private void callAppUsersService() {
        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getAppUsersAPI(accessToken).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    List<FindPeople> findPeopleList = response.body();
                    /*findPeopleAdapter.clearAll();
                    findPeopleAdapter.addItemsAll(findPeopleList);
                    lvFindPeople.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);
                    llNoPeople.setVisibility(View.GONE);*/
                    originalList = response.body();

                    loadAlphabetOrder(findPeopleList);

                    listView.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);
                    llNoPeople.setVisibility(View.GONE);

                } else {
                    noData.setVisibility(View.VISIBLE);
                    llNoPeople.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
                noData.setVisibility(View.VISIBLE);
                llNoPeople.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        menu1 = menu;
        if (isRepresentative) {
            searchPeople(menu);
        } else {
            Util.prepareTransferBalanceContactsSearch(TransferBalanceSelectContactActivity.this, menu, contactsListAdapter);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void loadAlphabetOrder(List<FindPeople> list) {

        Collections.sort(list, new Comparator<FindPeople>() {
            @Override
            public int compare(FindPeople lhs, FindPeople rhs) {
                return lhs.getFirst_name().toLowerCase().compareTo(rhs.getFirst_name().toLowerCase());
            }
        });

        contactsListAdapter.addItemsAll(list);
        invalidateOptionsMenu();
        //originalList = list;
        originalList.addAll(list);
        Helper.displayIndexTransferBalance(this, layout, originalList, listView);
    }

    private boolean isMoreLoading = false;

    private AbsListView.OnScrollListener onScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int threshold = 1;
                int count = listView.getCount();
                if (scrollState == SCROLL_STATE_IDLE) {
                    //if (isMoreLoading==false && listView.getLastVisiblePosition() >= count - threshold && searchView.isIconified() || TextUtils.isEmpty(searchView.getQuery())) {
                    if(searchView != null) {
                        if (isMoreLoading == false && listView.getLastVisiblePosition() >= count - threshold && searchView.isIconified() || TextUtils.isEmpty(searchView.getQuery()) && isRepresentative) {
                            //if (isMoreLoading==false && listView.getLastVisiblePosition() >= count - threshold && isRepresentative) {
                            doPagination();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                // do nothing
            }
        };
    }

    private void doPagination() {
        isMoreLoading = true;
        pageCount++;
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getRepresentativePeopleAPI(accessToken, pageCount, 30, true).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body().size() > 0) {
                    List<FindPeople> findPeopleList = response.body();
                    loadAlphabetOrder(findPeopleList);
                }
                isMoreLoading = false;
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
                isMoreLoading = false;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FindPeople contact = (FindPeople) listView.getItemAtPosition(position);
        Intent intent = new Intent(this, TransferBalanceActivity.class);
        intent.putExtra("balance", balance);
        intent.putExtra("currencySymbol", currencySymbol);
        intent.putExtra("name", contact.getFirst_name() + " " + contact.getLast_name());
        intent.putExtra("phoneNo", contact.getPhone_no());
        intent.putExtra("profilePic", contact.getAvatar());
        intent.putExtra("id", contact.getId());
        startActivityForResult(intent, 22);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 22 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void searchPeople(Menu menu) {
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem;

        searchMenuItem = menu.findItem(R.id.menu_search);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Enter atleast 6 characters...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setInputType(InputType.TYPE_CLASS_PHONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public static final String TAG = "Search in TransferBal";

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "onQueryTextChange: " + query);
                Util.hideKeyboard(TransferBalanceSelectContactActivity.this, TransferBalanceSelectContactActivity.this.getCurrentFocus());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "onQueryTextChange: " + newText);
                callSearchingService(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Util.hideKeyboard(TransferBalanceSelectContactActivity.this, TransferBalanceSelectContactActivity.this.getCurrentFocus());
                contactsListAdapter.clearAll();
                contactsListAdapter.addItemsAll(originalList);
                listView.setVisibility(View.VISIBLE);
                noData.setVisibility(View.GONE);
                llNoPeople.setVisibility(View.GONE);
                return true;
            }
        });
    }

    private void callSearchingService(String newText) {

        String searchKey = newText.trim();
        if (searchKey.isEmpty()) {
            contactsListAdapter.clearAll();
            contactsListAdapter.addItemsAll(originalList);
        } else if (searchKey.length() > 5) {
            String decodedString = searchKey;
            try {
                decodedString = URLDecoder.decode(searchKey, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            if (call != null) {
                call.cancel();
            }
            call = yoService.searchInBalanceTransferContacts(accessToken, decodedString, 1, 100);
            call.enqueue(new Callback<List<FindPeople>>() {
                @Override
                public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                    if (response.body() != null && response.body().size() > 0) {
                        List<FindPeople> findPeopleList = response.body();
                        contactsListAdapter.clearAll();
                        contactsListAdapter.addItemsAll(findPeopleList);
                        listView.setVisibility(View.VISIBLE);
                        noData.setVisibility(View.GONE);
                        llNoPeople.setVisibility(View.GONE);

                    } else {
                        noData.setVisibility(View.VISIBLE);
                        llNoPeople.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                    noData.setVisibility(View.VISIBLE);
                    llNoPeople.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menu1 != null) {
            Util.changeMenuItemsVisibility(menu1, R.id.menu_search, false);
            Util.registerSearchLister(this, menu1);
        }
        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        contactsListAdapter.clearAll();
        contactsListAdapter.addItemsAll(originalList);
        listView.setVisibility(View.VISIBLE);
        if (originalList.size() > 0) {
            noData.setVisibility(View.GONE);
            llNoPeople.setVisibility(View.GONE);
        }
    }

}
