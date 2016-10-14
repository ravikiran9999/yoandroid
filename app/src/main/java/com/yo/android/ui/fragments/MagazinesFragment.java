package com.yo.android.ui.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.FilterWithSpaceAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.Popup;
import com.yo.android.model.Topics;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.FindPeopleActivity;
import com.yo.android.ui.FollowersActivity;
import com.yo.android.ui.FollowingsActivity;
import com.yo.android.ui.MyCollections;
import com.yo.android.ui.NotificationsActivity;
import com.yo.android.ui.WishListActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by creatives on 6/27/2016.
 */
public class MagazinesFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    private List<Topics> topicsList;

    private Menu menu;
    public static List<Topics> unSelectedTopics;
    FilterWithSpaceAdapter<String> mAdapter;
    private boolean isAlreadyShown;
    private boolean isRemoved;

    public MagazineFlipArticlesFragment getmMagazineFlipArticlesFragment() {
        return mMagazineFlipArticlesFragment;
    }

    public void setmMagazineFlipArticlesFragment(MagazineFlipArticlesFragment mMagazineFlipArticlesFragment) {
        this.mMagazineFlipArticlesFragment = mMagazineFlipArticlesFragment;
    }

    private MagazineFlipArticlesFragment mMagazineFlipArticlesFragment;

    public MagazinesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_magazines, menu);
        this.menu = menu;
        prepareTopicsSearch(menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_magazines, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new FilterWithSpaceAdapter<String>(getActivity(),
                R.layout.textviewitem, new ArrayList<String>());
        if ((mMagazineFlipArticlesFragment = (MagazineFlipArticlesFragment) getChildFragmentManager().findFragmentById(R.id.bottom)) != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.bottom, mMagazineFlipArticlesFragment)
                    .commit();

        } else {
            mMagazineFlipArticlesFragment = new MagazineFlipArticlesFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.bottom, mMagazineFlipArticlesFragment)
                    .commit();
        }

        topicsList = new ArrayList<Topics>();
        unSelectedTopics = new ArrayList<>();

        callApiSearchTopics();
    }

    private void callApiSearchTopics() {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");

        yoService.tagsAPI(accessToken).enqueue(new Callback<List<Topics>>() {
            @Override
            public void onResponse(Call<List<Topics>> call, Response<List<Topics>> response) {
                dismissProgressDialog();
                if (getActivity() == null || response == null || response.body() == null) {
                    return;
                }
                topicsList.clear();
                topicsList.addAll(response.body());
                if (TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
                    List<String> followedTopicsIdsList = new ArrayList<String>();
                    for (int k = 0; k < topicsList.size(); k++) {
                        if (topicsList.get(k).isSelected()) {
                            followedTopicsIdsList.add(String.valueOf(topicsList.get(k).getId()));
                        }

                    }
                    preferenceEndPoint.saveStringPreference("magazine_tags", TextUtils.join(",", followedTopicsIdsList));
                }
                List<String> topicNamesList = new ArrayList<String>();
                for (int i = 0; i < topicsList.size(); i++) {
                    //if (topicsList.get(i).isSelected()) {
                    topicNamesList.add(topicsList.get(i).getName());
                    //}
                }
                mAdapter.clear();
                mAdapter.addAll(topicNamesList);
                mAdapter.notifyDataSetChanged();
                unSelectedTopics.clear();

                for (int i = 0; i < topicsList.size(); i++) {
                    if (!topicsList.get(i).isSelected()) {
                        unSelectedTopics.add(topicsList.get(i));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Topics>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

    public void refreshSearch() {
        callApiSearchTopics();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.menu_create_magazines:
                Intent createMagazinesIntent = new Intent(getActivity(), CreateMagazineActivity.class);
                startActivity(createMagazinesIntent);
                break;
            case R.id.menu_my_collections:
                Intent myCollectionsIntent = new Intent(getActivity(), MyCollections.class);
                startActivity(myCollectionsIntent);
                break;

            case R.id.menu_find_people:
                Intent findPeopleIntent = new Intent(getActivity(), FindPeopleActivity.class);
                startActivity(findPeopleIntent);
                break;
            case R.id.menu_followers:
                Intent followersIntent = new Intent(getActivity(), FollowersActivity.class);
                startActivity(followersIntent);
                break;
            case R.id.menu_wish_list:
                Intent wishListIntent = new Intent(getActivity(), WishListActivity.class);
                startActivity(wishListIntent);
                break;
            case R.id.menu_followings:
                Intent followingstIntent = new Intent(getActivity(), FollowingsActivity.class);
                startActivity(followingstIntent);
                break;
            default:
                break;
        }
        return true;
    }

    private void prepareTopicsSearch(Menu menu) {
        SearchView search = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        search.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        final SearchView.SearchAutoComplete searchTextView = (SearchView.SearchAutoComplete) search.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    mMagazineFlipArticlesFragment.lastReadArticle = 0;

                    return false;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {

                    mMagazineFlipArticlesFragment.lastReadArticle = 0;

                    return false;
                }
            });

            searchTextView.setTextColor(Color.WHITE);
            searchTextView.setThreshold(1);
            searchTextView.setAdapter(mAdapter);
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            //This sets the cursor resource ID to 0 or @null which will make it visible on white background
            mCursorDrawableRes.set(searchTextView, R.drawable.red_cursor);

            searchTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("Search", "The selected item is " + parent.getItemAtPosition(position));
                    String topicName = (String) parent.getItemAtPosition(position);
                    searchTextView.setText(topicName);
                    searchTextView.setSelection(topicName.trim().length());
                    String topicId = "";
                    for (int i = 0; i < topicsList.size(); i++) {
                        if (topicsList.get(i).getName().equals(topicName)) {
                            topicId = topicsList.get(i).getId();
                            break;
                        }
                    }
                    if (getActivity() != null) {
                        Util.hideKeyboard(getActivity(), searchTextView);
                    }
                    List<String> tagIds = new ArrayList<String>();
                    tagIds.add(topicId);
                    if (mMagazineFlipArticlesFragment != null) {
                        mMagazineFlipArticlesFragment.lastReadArticle = 0;
                        mMagazineFlipArticlesFragment.loadArticles(tagIds);
                    }

                    return;
                }

            });
            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    if (mMagazineFlipArticlesFragment != null) {
                        mMagazineFlipArticlesFragment.lastReadArticle = 0;
                        //mMagazineFlipArticlesFragment.loadArticles(null);
                        mMagazineFlipArticlesFragment.getCachedArticles();

                    }
                    return true;
                }
            });

            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                public static final String TAG = "Search in Magazines";

                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.i(TAG, "onQueryTextSubmit: " + query);
                    if (mAdapter.getCount() > 0) {
                        Log.d("Search", "The selected item is " + mAdapter.getItem(0));
                        String topicName = (String) mAdapter.getItem(0);
                        searchTextView.setText(topicName);
                        searchTextView.setSelection(topicName.trim().length());
                        String topicId = "";
                        for (int i = 0; i < topicsList.size(); i++) {
                            if (topicsList.get(i).getName().equals(topicName)) {
                                topicId = topicsList.get(i).getId();
                                break;
                            }
                        }
                        if (getActivity() != null) {
                            Util.hideKeyboard(getActivity(), searchTextView);
                        }
                        searchTextView.dismissDropDown();
                        List<String> tagIds = new ArrayList<String>();
                        tagIds.add(topicId);
                        if (mMagazineFlipArticlesFragment != null) {
                            mMagazineFlipArticlesFragment.lastReadArticle = 0;
                            mMagazineFlipArticlesFragment.loadArticles(tagIds);
                        }
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.i(TAG, "onQueryTextChange: " + newText);
                    return true;
                }
            });

        } catch (Exception e) {
            // do nothing
        }
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(((BottomTabsActivity)getActivity()).getSupportActionBar().getTitle().equals(getString(R.string.magazines))) {
            if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                if(!isRemoved) {
                Type type = new TypeToken<List<Popup>>() {
                }.getType();
                List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                if (!isAlreadyShown) {
                PopupHelper.getPopup(PopupHelper.PopupsEnum.MAGAZINES, popup, getActivity(), preferenceEndPoint, this, this);
                    isAlreadyShown = true;
                }
                } else {
                    isRemoved = false;
                }
            }
        }
    }

    public void onEventMainThread(String action) {
        if (Constants.REFRESH_TOPICS_ACTION.equals(action)) {
            callApiSearchTopics();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                Type type = new TypeToken<List<Popup>>() {
                }.getType();
                List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                if (!isAlreadyShown) {
                PopupHelper.getPopup(PopupHelper.PopupsEnum.MAGAZINES, popup, getActivity(), preferenceEndPoint, this, this);
                    isAlreadyShown = true;
                }
            }
        }
        else {
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
