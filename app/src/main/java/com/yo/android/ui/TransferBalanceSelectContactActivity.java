package com.yo.android.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
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
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.helpers.Helper;
import com.yo.android.model.Contact;
import com.yo.android.model.FindPeople;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used to display the users to which the logged in user can transfer his balance to
 */
public class TransferBalanceSelectContactActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @Inject
    ContactsSyncManager mContactsSyncManager;


    private TransferBalanceContactAdapter contactsListAdapter;
    private ListView listView;
    private ListView layout;
    private int pageCount = 1;
    private boolean isRepresentative;
    private String balance;
    private String currencySymbol;
    private Menu menu1;
    private SearchView searchView;
    private List<Contact> originalList;
    private Call<List<FindPeople>> call;
    private TextView noData;
    private LinearLayout llNoPeople;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, TransferBalanceSelectContactActivity.class);
        activity.startActivityForResult(intent, 33);
    }

    private static Intent createIntent(Activity activity, String availableBalance, boolean userType) {
        Intent intent = new Intent(activity, TransferBalanceSelectContactActivity.class);
        intent.putExtra(Constants.CURRENT_BALANCE, availableBalance);
        intent.putExtra(Constants.USER_TYPE, userType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_balance_select_contact);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getString(R.string.select_contact);

        getSupportActionBar().setTitle(title);

        balance = getIntent().getStringExtra(Constants.CURRENT_BALANCE);
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
        isRepresentative = preferenceEndPoint.getBooleanPreference(Constants.USER_TYPE);
        handleRepresentative(isRepresentative);
    }

    /**
     * Calls the respective service based on whether the user is a representative or not
     *
     * @param isRepresentative isRepresentative or not
     */
    private void handleRepresentative(boolean isRepresentative) {

        //callAppUsersService();
        /*if (isRepresentative) {
            //callFindPeopleService();
        } else {
            callAppUsersService();
        }*/

        callAppUsersService();
    }

    /**
     * Calls the service to get the users who are representatives
     */
    private void callFindPeopleService() {
        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
       /* yoService.getRepresentativePeopleAPI(accessToken, 1, 30, true).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    List<FindPeople> findPeopleList = response.body();
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
        });*/
    }

    /**
     * Calls the service to get the app users
     */
    private void callAppUsersService() {
        final List<Contact> contacts = mContactsSyncManager.getContacts();

        if (contacts != null && !contacts.isEmpty()) {
            loadAlphabetOrder(contacts);

        } else {

            //TODO this code can be removed
            if (!isFinishing()) {
                showProgressDialog();
                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                yoService.getAppUsersAPI(accessToken).enqueue(new Callback<List<FindPeople>>() {
                    @Override
                    public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                        dismissProgressDialog();
                        if (response.body() != null && response.body().size() > 0) {
                            try {
                                List<FindPeople> findPeopleList = response.body();

                                //originalList = response.body();

                                //loadAlphabetOrder(findPeopleList);

                                listView.setVisibility(View.VISIBLE);
                                noData.setVisibility(View.GONE);
                                llNoPeople.setVisibility(View.GONE);
                            } finally {
                                if(response != null && response.body() != null) {
                                    try {
                                        response.body().clear();
                                        response = null;
                                    }catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

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
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        menu1 = menu;
        if (isRepresentative) {
            searchPeople(menu);
        } else {
            Util.prepareTransferBalanceContactsSearch(TransferBalanceSelectContactActivity.this, menu, contactsListAdapter, noData, listView, llNoPeople);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Loads the contacts in alphabetical order
     *
     * @param contactList The list of users
     */
    private void loadAlphabetOrder(List<Contact> contactList) {
        List<Contact> yoContactsList = new ArrayList<>();

        for (Contact contact : contactList) {
            if (contact.isYoAppUser()) {
                yoContactsList.add(contact);
            }
        }
        contactList = yoContactsList;

        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            }
        });

        contactsListAdapter.addItemsAll(contactList);
        invalidateOptionsMenu();
        originalList.addAll(contactList);
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
                    if (searchView != null) {
                        if (isMoreLoading == false && listView.getLastVisiblePosition() >= count - threshold && searchView.isIconified() || TextUtils.isEmpty(searchView.getQuery()) && isRepresentative) {
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

    /**
     * Gets the next page of representatives using pagination
     */
    private void doPagination() {
        isMoreLoading = true;
        pageCount++;
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getRepresentativePeopleAPI(accessToken, pageCount, 30, true).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                try {
                    dismissProgressDialog();
                    if (response.body().size() > 0) {
                        List<FindPeople> findPeopleList = response.body();
                        //loadAlphabetOrder(findPeopleList);
                    }
                    isMoreLoading = false;
                }finally {
                    if(response != null && response.body() != null) {
                        try {
                            response.body().clear();
                            response = null;
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
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
        Contact contact = (Contact) listView.getItemAtPosition(position);
        Intent intent = new Intent();
        intent.putExtra(Constants.SELECTED_CONTACT_TO_TRANSFER, contact);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 22 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * Searches for the user in the list of users based on the search text
     *
     * @param menu
     */
    private void searchPeople(Menu menu) {
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        Util.hideSideIndex(searchMenuItem, layout);

        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Enter atleast 4 characters...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setInputType(InputType.TYPE_CLASS_PHONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Util.hideKeyboard(TransferBalanceSelectContactActivity.this, TransferBalanceSelectContactActivity.this.getCurrentFocus());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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

    /**
     * Calls the service to search for a user based on the search text
     *
     * @param newText
     */
    private void callSearchingService(String newText) {

        String searchKey = newText.trim();
        if (searchKey.isEmpty()) {
            contactsListAdapter.clearAll();
            contactsListAdapter.addItemsAll(originalList);
        } else if (searchKey.length() > 3) {
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
            Call<List<Contact>> call = yoService.searchInBalanceTransferContacts(accessToken, decodedString);
            call.enqueue(new Callback<List<Contact>>() {
                @Override
                public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                    if (response.body() != null && response.body().size() > 0) {
                        try {
                            List<Contact> contactList = response.body();
                            contactsListAdapter.clearAll();
                            contactsListAdapter.addItemsAll(contactList);
                            listView.setVisibility(View.VISIBLE);
                            noData.setVisibility(View.GONE);
                            llNoPeople.setVisibility(View.GONE);
                        } finally {
                            if(response != null && response.body() != null) {
                                try {
                                    response.body().clear();
                                    response = null;
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    } else {
                        noData.setVisibility(View.VISIBLE);
                        llNoPeople.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<List<Contact>> call, Throwable t) {
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

    /**
     * Refreshes the list of users
     */
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