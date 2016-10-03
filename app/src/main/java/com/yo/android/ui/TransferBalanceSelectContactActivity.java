package com.yo.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yo.android.R;
import com.yo.android.adapters.ContactsListAdapter;
import com.yo.android.adapters.TransferBalanceContactAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.helpers.Helper;
import com.yo.android.model.Contact;
import com.yo.android.model.FindPeople;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

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

        contactsListAdapter = new TransferBalanceContactAdapter(this, preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        listView.setAdapter(contactsListAdapter);
        listView.setOnScrollListener(onScrollListener());
        listView.setOnItemClickListener(this);

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

                }
            }

            @Override
            public void onFailure(Call<UserProfileInfo> call, Throwable t) {

            }
        });
    }

    private void handleRepresentative(boolean isRepresentative) {
        if(isRepresentative) {
            callFindPeopleService();
        } else {
            callAppUsersService();
        }
    }

    private void callFindPeopleService() {
        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getFindPeopleAPI(accessToken, 1, 30).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    List<FindPeople> findPeopleList = response.body();
                    /*findPeopleAdapter.clearAll();
                    findPeopleAdapter.addItemsAll(findPeopleList);
                    lvFindPeople.setVisibility(View.VISIBLE);
                    noData.setVisibility(View.GONE);
                    llNoPeople.setVisibility(View.GONE);
                    originalList = response.body();*/

                    loadAlphabetOrder(findPeopleList);

                } else {
                    /*noData.setVisibility(View.VISIBLE);
                    llNoPeople.setVisibility(View.VISIBLE);
                    lvFindPeople.setVisibility(View.GONE);*/
                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
               /* noData.setVisibility(View.VISIBLE);
                llNoPeople.setVisibility(View.VISIBLE);
                lvFindPeople.setVisibility(View.GONE);*/
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
                    llNoPeople.setVisibility(View.GONE);
                    originalList = response.body();*/

                    loadAlphabetOrder(findPeopleList);

                } else {
                    /*noData.setVisibility(View.VISIBLE);
                    llNoPeople.setVisibility(View.VISIBLE);
                    lvFindPeople.setVisibility(View.GONE);*/
                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        /*menu1 = menu;
        searchPeople(menu);*/
        return super.onCreateOptionsMenu(menu);
    }

    private void loadAlphabetOrder(List<FindPeople> list) {

        Collections.sort(list, new Comparator<FindPeople>() {
            @Override
            public int compare(FindPeople lhs, FindPeople rhs) {
                return lhs.getFirst_name().toLowerCase().compareTo(rhs.getFirst_name().toLowerCase());
            }
        });

        contactsListAdapter.addItems(list);
        Helper.displayIndexTransferBalance(this, layout, list, listView);
    }
    private boolean isMoreLoading=false;
    private AbsListView.OnScrollListener onScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int threshold = 1;
                int count = listView.getCount();
                if (scrollState == SCROLL_STATE_IDLE) {
                    //if (isMoreLoading==false && listView.getLastVisiblePosition() >= count - threshold && searchView.isIconified() || TextUtils.isEmpty(searchView.getQuery())) {
                    if (isMoreLoading==false && listView.getLastVisiblePosition() >= count - threshold && isRepresentative) {
                        doPagination();
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
        isMoreLoading=true;
        pageCount++;
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getFindPeopleAPI(accessToken, pageCount, 30).enqueue(new Callback<List<FindPeople>>() {
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
            startActivity(intent);
    }

}
