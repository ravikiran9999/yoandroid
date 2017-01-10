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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
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
    //private boolean isRemoved;
    List<String> topicsNames;
    private List<Topics> topicsNewList;

    @Bind(R.id.no_search_results)
    protected TextView noSearchResults;

    @Bind(R.id.bottom)
    protected FrameLayout layout;

    private List<String> topicNamesList = new ArrayList<String>();


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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_magazines, menu);
        this.menu = menu;

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

        if (getActivity().getIntent().hasExtra("tagIds")) {
            List<String> followedTopicsIdsList = getActivity().getIntent().getStringArrayListExtra("tagIds");
            addTopics(followedTopicsIdsList);
        } else {
            callApiSearchTopics();
        }
    }

    public void update() {
        if (mMagazineFlipArticlesFragment != null) {
            Log.d("MagazinesFragment", "In update() MagazinesFragment");
            mMagazineFlipArticlesFragment.update();
        }
    }

    public void removeReadArticles() {
        if (mMagazineFlipArticlesFragment != null) {
            Log.d("MagazinesFragment", "In removeReadArticles() MagazinesFragment");
            mMagazineFlipArticlesFragment.removeReadArticles();
        }
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
                topicsNewList = topicsList;
                if (TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
                    List<String> followedTopicsIdsList = new ArrayList<String>();
                    for (int k = 0; k < topicsList.size(); k++) {
                        if (topicsList.get(k).isSelected()) {
                            followedTopicsIdsList.add(String.valueOf(topicsList.get(k).getId()));
                        }

                    }
                    preferenceEndPoint.saveStringPreference("magazine_tags", TextUtils.join(",", followedTopicsIdsList));
                }
                topicNamesList = new ArrayList<String>();
                for (int i = 0; i < topicsList.size(); i++) {
                    //if (topicsList.get(i).isSelected()) {
                    topicNamesList.add(topicsList.get(i).getName());
                    //}
                }
                mAdapter.clear();
                mAdapter.addAll(topicNamesList);
                mAdapter.notifyDataSetChanged();
                topicsNames = topicNamesList;
                getActivity().invalidateOptionsMenu();
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
        prepareTopicsSearch(menu);

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
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    mMagazineFlipArticlesFragment.lastReadArticle = 0;
                    mMagazineFlipArticlesFragment.refresh();
                    noSearchResults.setVisibility(View.GONE);
                    return true;
                }
            });

            searchTextView.setTextColor(Color.WHITE);
            searchTextView.setThreshold(1);
            if (!topicsNames.isEmpty() && mAdapter.getCount() == 0) {
                mAdapter.addAll(topicsNames);
                mAdapter.notifyDataSetChanged();
            }
            if (topicsList.isEmpty()) {
                topicsList = topicsNewList;
            }
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
                        //mMagazineFlipArticlesFragment.getCachedArticles();
                        mMagazineFlipArticlesFragment.getLandingCachedArticles();

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
                    boolean isContain = false;
                    for (String item : topicNamesList) {
                        if (item.toLowerCase().contains(newText.toLowerCase())) {
                            noSearchResults.setVisibility(View.GONE);
                            layout.setVisibility(View.VISIBLE);
                            isContain = true;
                            break;
                        }
                    }
                    if (!isContain) {
                        noSearchResults.setVisibility(View.VISIBLE);
                        layout.setVisibility(View.GONE);
                    }
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
        if (getActivity() instanceof BottomTabsActivity) {
            BottomTabsActivity activity = (BottomTabsActivity) getActivity();
            if (activity.getFragment() instanceof MagazinesFragment) {

                if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                    //if (!isRemoved) {
                    Type type = new TypeToken<List<Popup>>() {
                    }.getType();
                    List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                    if (popup != null) {
                        for (Popup p : popup) {
                            if (p.getPopupsEnum() == PopupHelper.PopupsEnum.MAGAZINES) {
                                if (!isAlreadyShown) {
                                    //PopupHelper.getPopup(PopupHelper.PopupsEnum.MAGAZINES, popup, getActivity(), preferenceEndPoint, this, this);
                                    PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.MAGAZINES, p, getActivity(), preferenceEndPoint, this, this, popup);
                                    isAlreadyShown = true;
                                    break;
                                }
                            }
                        }
                    }
                        /*if (popup != null && popup.size() > 0 && popup.get(0).getPopupsEnum() == PopupHelper.PopupsEnum.MAGAZINES) {
                            if (!isAlreadyShown) {
                                PopupHelper.getPopup(PopupHelper.PopupsEnum.MAGAZINES, popup, getActivity(), preferenceEndPoint, this, this);
                                isAlreadyShown = true;
                            }
                        }*/
                   /* } else {
                        isRemoved = false;
                    }*/
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
            if (getActivity() instanceof BottomTabsActivity) {
                BottomTabsActivity activity = (BottomTabsActivity) getActivity();
                if (activity.getFragment() instanceof MagazinesFragment) {
                    if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                        Type type = new TypeToken<List<Popup>>() {
                        }.getType();
                        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                        if (popup != null) {
                            isAlreadyShown = false;
                            for (Popup p : popup) {
                                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.MAGAZINES) {
                                    if (!isAlreadyShown) {
                                        //PopupHelper.getPopup(PopupHelper.PopupsEnum.MAGAZINES, popup, getActivity(), preferenceEndPoint, this, this);
                                        PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.MAGAZINES, p, getActivity(), preferenceEndPoint, this, this, popup);
                                        isAlreadyShown = true;
                                        break;
                                    }
                                }
                            }
                        }
                        /*if (popup != null && popup.size() > 0 && popup.get(0).getPopupsEnum() == PopupHelper.PopupsEnum.MAGAZINES) {
                            if (!isAlreadyShown) {
                                PopupHelper.getPopup(PopupHelper.PopupsEnum.MAGAZINES, popup, getActivity(), preferenceEndPoint, this, this);
                                isAlreadyShown = true;
                            }
                        }*/
                    }
                }
            }

        } else {
        }
    }

    @Override
    public void closePopup() {
        //isAlreadyShown = false;
        //isRemoved = true;
        //preferenceEndPoint.removePreference(Constants.POPUP_NOTIFICATION);
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        if (popup != null) {
            List<Popup> tempPopup = new ArrayList<>(popup);
            for (Popup p : popup) {
                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.MAGAZINES) {
                    tempPopup.remove(p);
                    break;
                }
            }
            popup = tempPopup;
        }
        //popup.remove(0);
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }

    private void addTopics(final List<String> followedTopicsIdsList) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.addTopicsAPI(accessToken, followedTopicsIdsList).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                /*if ("Magazines".equals(from)) {
                    Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, MyCollections.class);
                    startActivity(myCollectionsIntent);
                    finish();
                } else */
                if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
                    //TODO:Disalbe flag for Follow more
                    preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN, false);
                    callApiSearchTopics();
                } /*else {
                    Intent intent = new Intent();
                    setResult(2, intent);
                    finish();
                }*/

                preferenceEndPoint.saveStringPreference("magazine_tags", TextUtils.join(",", followedTopicsIdsList));
                if (followedTopicsIdsList.isEmpty()) {
                    mMagazineFlipArticlesFragment.getLandingCachedArticles();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Error while adding topics", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        Log.d("MagazinesFragment", "In onOptionsMenuClosed()");
    }


}
