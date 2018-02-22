package com.yo.android.ui.fragments;

import android.app.Activity;
import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.adapters.FilterWithSpaceAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.PopupHelper;
import com.yo.android.model.Categories;
import com.yo.android.model.Popup;
import com.yo.android.model.Topics;
import com.yo.android.ui.BottomTabsActivity;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.FindPeopleActivity;
import com.yo.android.ui.FollowersActivity;
import com.yo.android.ui.FollowingsActivity;
import com.yo.android.ui.MagazineActivity;
import com.yo.android.ui.MyCollections;
import com.yo.android.ui.WishListActivity;
import com.yo.android.usecase.MagazinesFlipArticlesUsecase;
import com.yo.android.usecase.MagazinesServicesUsecase;
import com.yo.android.util.Constants;
import com.yo.android.util.MagazineDashboardHelper;
import com.yo.android.util.PopupDialogListener;
import com.yo.android.util.Util;
import com.yo.android.util.YODialogs;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * This fragment is used to show the Magazines Search and attaches the fragment to load the magazine articles
 */
public class MagazinesFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener, PopupDialogListener {

    @Bind(R.id.no_search_results)
    protected TextView noSearchResults;
    @Bind(R.id.bottom)
    protected FrameLayout layout;

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    public ArrayList<Categories> categoriesList;
    public static ArrayList<Categories> newCategoriesList;
    private List<Topics> topicsList;
    private Menu menu;
    public static List<Topics> unSelectedTopics;
    private FilterWithSpaceAdapter<String> mAdapter;
    private boolean isAlreadyShown;
    List<String> topicsNames;
    private List<Topics> topicsNewList;
    private List<String> topicNamesList = new ArrayList<String>();
    private boolean isSharedPreferenceShown;

    private boolean isEventLogged;
    private Activity activity;
    private MagazineFlipArticlesFragment mMagazineFlipArticlesFragment;
    @Inject
    MagazinesServicesUsecase magazinesServicesUsecase;
    @Inject
    public MagazinesFlipArticlesUsecase magazinesFlipArticlesUsecase;
    MagazineDashboardHelper magazineDashboardHelper;


    public MagazineFlipArticlesFragment getmMagazineFlipArticlesFragment() {
        return mMagazineFlipArticlesFragment;
    }

    public void setmMagazineFlipArticlesFragment(MagazineFlipArticlesFragment mMagazineFlipArticlesFragment) {
        this.mMagazineFlipArticlesFragment = mMagazineFlipArticlesFragment;
    }


    public MagazinesFragment() {
        // Required empty public constructor
        magazineDashboardHelper = new MagazineDashboardHelper();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setHasOptionsMenu(true);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);

        if (!isEventLogged) {
            if (getActivity() instanceof BottomTabsActivity) {
                BottomTabsActivity activity = (BottomTabsActivity) getActivity();
                if (activity.getFragment() instanceof MagazinesFragment) {
                    // Capture user id
                    Map<String, String> magazinesParams = new HashMap<String, String>();
                    String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                    //param keys and values have to be of String type
                    magazinesParams.put("UserId", userId);

                    FlurryAgent.logEvent("Magazines", magazinesParams, true);
                }
            }
        }
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
        menu.clear();
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

        categoriesList = new ArrayList<>();
        newCategoriesList = new ArrayList<>();
        topicsList = new ArrayList<Topics>();
        unSelectedTopics = new ArrayList<>();

        if (getActivity().getIntent().hasExtra("tagIds")) {
            List<String> followedTopicsIdsList = getActivity().getIntent().getStringArrayListExtra("tagIds");
            addTopics(followedTopicsIdsList);
        } else {
            //callApiSearchTopics();
            if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                callApiSearchTopics();
            } else {
                callApiSearchTopics();
                getRandomTopics();
            }
        }
    }

    public void update() {
        if (mMagazineFlipArticlesFragment != null) {
            mMagazineFlipArticlesFragment.update();
        }
    }

    public void removeReadArticles() {
        if (mMagazineFlipArticlesFragment != null) {
            Log.d("MagazinesFragment", "In removeReadArticles() MagazinesFragment");
            mMagazineFlipArticlesFragment.removeReadArticles();
        }
    }

    /**
     * Getting all the articles topics
     */
    private void callApiSearchTopics() {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.tagsAPI(accessToken).enqueue(new Callback<List<Topics>>() {
            @Override
            public void onResponse(Call<List<Topics>> call, Response<List<Topics>> response) {
                try {
                    dismissProgressDialog();
                    if (getActivity() == null || response == null || response.body() == null) {
                        return;
                    }
                    topicsList.clear();
                    topicsList.addAll(response.body());
                    topicsNewList = topicsList;
                    if (TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.MAGAZINE_TAGS))) {
                        List<String> followedTopicsIdsList = new ArrayList<String>();
                        for (int k = 0; k < topicsList.size(); k++) {
                            if (topicsList.get(k).isSelected()) {
                                followedTopicsIdsList.add(String.valueOf(topicsList.get(k).getId()));
                            }

                        }
                        preferenceEndPoint.saveStringPreference(Constants.MAGAZINE_TAGS, TextUtils.join(",", followedTopicsIdsList));
                    }
                    topicNamesList = new ArrayList<String>();

                    /*for (int i = 0; i < topicsList.size(); i++) {
                        topicNamesList.add(topicsList.get(i).getName());
                    }*/

                    // Improve performance
                    for (Topics topics : topicsList) {
                        topicNamesList.add(topics.getName());
                    }

                    mAdapter.clear();
                    mAdapter.addAll(topicNamesList);
                    mAdapter.notifyDataSetChanged();
                    topicsNames = topicNamesList;
                    getActivity().invalidateOptionsMenu();
                    unSelectedTopics.clear();

                    /*for (int i = 0; i < topicsList.size(); i++) {
                        if (!topicsList.get(i).isSelected()) {
                            unSelectedTopics.add(topicsList.get(i));
                        }
                    }*/

                    for(Topics topics : topicsList) {
                        if(topics.isSelected()) {
                            unSelectedTopics.add(topics);
                        }
                    }

                } finally {
                    if (response != null && response.body() != null) {
                        response.body().clear();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Topics>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

    private void getRandomTopics() {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.randomTagsAPI(accessToken).enqueue(new Callback<List<Categories>>() {
            @Override
            public void onResponse(Call<List<Categories>> call, Response<List<Categories>> response) {
                try {
                    dismissProgressDialog();
                    if (getActivity() == null || response == null || response.body() == null) {
                        return;
                    }
                    categoriesList.clear();
                    categoriesList.addAll(response.body());
                    newCategoriesList.addAll(categoriesList);
                } finally {
                    if (response != null && response.body() != null) {
                        try {
                            response.body().clear();
                            response = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Categories>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

    public void refreshSearch() {
        //callApiSearchTopics();
        if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
            callApiSearchTopics();
        } else {
            callApiSearchTopics();
            getRandomTopics();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        prepareTopicsSearch(menu);
        boolean renewalStatus = preferenceEndPoint.getBooleanPreference(Constants.MAGAZINE_LOCK, false);
        switch (item.getItemId()) {
            case R.id.menu_move_to_first:
                //mMagazineFlipArticlesFragment.getLandingCachedArticles();
                mMagazineFlipArticlesFragment.moveToFirst();
                break;
            case R.id.menu_create_magazines:
                if (!renewalStatus) {
                    Intent createMagazinesIntent = new Intent(getActivity(), CreateMagazineActivity.class);
                    startActivity(createMagazinesIntent);
                } else {
                    YODialogs.renewMagazine(getActivity(), null, R.string.renewal_message, preferenceEndPoint);
                }
                break;
            case R.id.menu_my_collections:
                if (!renewalStatus) {
                    Intent myCollectionsIntent = new Intent(getActivity(), MyCollections.class);
                    startActivity(myCollectionsIntent);
                } else {
                    YODialogs.renewMagazine(getActivity(), null, R.string.renewal_message, preferenceEndPoint);
                }
                break;

            case R.id.menu_find_people:
                Intent findPeopleIntent = new Intent(getActivity(), FindPeopleActivity.class);
                startActivity(findPeopleIntent);
                break;
            case R.id.menu_followers:
                if (!renewalStatus) {
                    Intent followersIntent = new Intent(getActivity(), FollowersActivity.class);
                    startActivity(followersIntent);
                } else {
                    YODialogs.renewMagazine(getActivity(), null, R.string.renewal_message, preferenceEndPoint);
                }
                break;
            case R.id.menu_wish_list:
                if (!renewalStatus) {
                    Intent wishListIntent = new Intent(getActivity(), WishListActivity.class);
                    startActivity(wishListIntent);
                } else {
                    YODialogs.renewMagazine(getActivity(), null, R.string.renewal_message, preferenceEndPoint);
                }
                break;
            case R.id.menu_followings:
                if (!renewalStatus) {
                    Intent followingstIntent = new Intent(getActivity(), FollowingsActivity.class);
                    startActivity(followingstIntent);
                } else {
                    YODialogs.renewMagazine(getActivity(), null, R.string.renewal_message, preferenceEndPoint);
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Handling searching of topics
     *
     * @param menu
     */
    private void prepareTopicsSearch(Menu menu) {
        SearchView search = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        search.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        final SearchView.SearchAutoComplete searchTextView = (SearchView.SearchAutoComplete) search.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    MagazineFlipArticlesFragment.lastReadArticle = 0;
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    MagazineFlipArticlesFragment.lastReadArticle = 0;
                    mMagazineFlipArticlesFragment.refresh();
                    noSearchResults.setVisibility(View.GONE);
                    getActivity().invalidateOptionsMenu();
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
                        MagazineFlipArticlesFragment.lastReadArticle = 0;
                        magazinesServicesUsecase.loadArticles(tagIds, false, getActivity(), mMagazineFlipArticlesFragment);
                    }

                    return;
                }

            });
            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    if (mMagazineFlipArticlesFragment != null) {
                        MagazineFlipArticlesFragment.lastReadArticle = 0;
                        magazinesFlipArticlesUsecase.getLandingCachedArticles(getActivity(), mMagazineFlipArticlesFragment.myBaseAdapter, mMagazineFlipArticlesFragment, magazineDashboardHelper );
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
                            MagazineFlipArticlesFragment.lastReadArticle = 0;
                            magazinesServicesUsecase.loadArticles(tagIds, false, getActivity(), mMagazineFlipArticlesFragment);
                        }
                        mAdapter.clear();
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
                    } else {
                        noSearchResults.setVisibility(View.GONE);
                        layout.setVisibility(View.VISIBLE);
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
                    Type type = new TypeToken<List<Popup>>() {
                    }.getType();
                    List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                    if (popup != null) {
                        for (Popup p : popup) {
                            if (p.getPopupsEnum() == PopupHelper.PopupsEnum.MAGAZINES) {
                                if (!isAlreadyShown) {
                                    PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.MAGAZINES, p, getActivity(), preferenceEndPoint, this, this, popup);
                                    isAlreadyShown = true;
                                    isSharedPreferenceShown = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public void onEventMainThread(String action) {
        if (Constants.REFRESH_TOPICS_ACTION.equals(action)) {
            //callApiSearchTopics();
            if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                callApiSearchTopics();
            } else {
                callApiSearchTopics();
                getRandomTopics();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (preferenceEndPoint != null) {
                // Capture user id
                Map<String, String> magazinesParams = new HashMap<String, String>();
                String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                //param keys and values have to be of String type
                magazinesParams.put("UserId", userId);

                FlurryAgent.logEvent("Magazines", magazinesParams, true);
                isEventLogged = true;
            }

            if (getActivity() instanceof BottomTabsActivity) {
                BottomTabsActivity activity = (BottomTabsActivity) getActivity();
                if (activity.getFragment() instanceof MagazinesFragment) {
                    if (preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION) != null) {
                        Type type = new TypeToken<List<Popup>>() {
                        }.getType();
                        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
                        if (popup != null) {
                            Collections.reverse(popup);
                            isAlreadyShown = false;
                            for (Popup p : popup) {
                                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.MAGAZINES) {
                                    if (!isAlreadyShown) {
                                        PopupHelper.getSinglePopup(PopupHelper.PopupsEnum.MAGAZINES, p, getActivity(), preferenceEndPoint, this, this, popup);
                                        isAlreadyShown = true;
                                        isSharedPreferenceShown = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {
        }
    }

    @Override
    public void closePopup() {
        Type type = new TypeToken<List<Popup>>() {
        }.getType();
        List<Popup> popup = new Gson().fromJson(preferenceEndPoint.getStringPreference(Constants.POPUP_NOTIFICATION), type);
        if (popup != null) {
            if (!isSharedPreferenceShown) {
                Collections.reverse(popup);
            }
            List<Popup> tempPopup = new ArrayList<>(popup);
            for (Popup p : popup) {
                if (p.getPopupsEnum() == PopupHelper.PopupsEnum.MAGAZINES) {
                    tempPopup.remove(p);
                    break;
                }
            }
            popup = tempPopup;
        }
        preferenceEndPoint.saveStringPreference(Constants.POPUP_NOTIFICATION, new Gson().toJson(popup));
    }

    /**
     * Adding the topics selected
     *
     * @param followedTopicsIdsList The followed topics list
     */
    private void addTopics(final List<String> followedTopicsIdsList) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.addTopicsAPI(accessToken, followedTopicsIdsList, "").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
                    //TODO:Disalbe flag for Follow more
                    preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN, false);
                    //callApiSearchTopics();
                    if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                        callApiSearchTopics();
                    } else {
                        callApiSearchTopics();
                        getRandomTopics();
                    }
                }
                preferenceEndPoint.saveStringPreference(Constants.MAGAZINE_TAGS, TextUtils.join(",", followedTopicsIdsList));
                if (followedTopicsIdsList.isEmpty()) {
                    magazinesFlipArticlesUsecase.getLandingCachedArticles(getActivity(), mMagazineFlipArticlesFragment.myBaseAdapter, mMagazineFlipArticlesFragment, magazineDashboardHelper );
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
    }
}
