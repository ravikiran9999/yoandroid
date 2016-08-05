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
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.model.Topics;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.FindPeopleActivity;
import com.yo.android.ui.FollowersActivity;
import com.yo.android.ui.FollowingsActivity;
import com.yo.android.ui.MyCollections;
import com.yo.android.ui.WishListActivity;
import com.yo.android.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by creatives on 6/27/2016.
 */
public class MagazinesFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    private List<Topics> topicsList;

    private ArrayAdapter mAdapter;
    private Menu menu;

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
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
        //MagazineTopicsSelectionFragment fragment = new MagazineTopicsSelectionFragment();
        //getChildFragmentManager().beginTransaction().add(R.id.top, fragment).commit();

        mAdapter = new ArrayAdapter<String>
                (getActivity(), R.layout.textviewitem, new ArrayList<String>());
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

        callApiSearchTopics();
    }

    private void callApiSearchTopics() {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        //showProgressDialog();
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
                    if (topicsList.get(i).isSelected()) {
                        topicNamesList.add(topicsList.get(i).getName());
                    }
                }
                mAdapter.clear();
                mAdapter.addAll(topicNamesList);
                mAdapter.notifyDataSetChanged();
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
                    return false;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {

                    return false;
                }
            });

            searchTextView.setTextColor(Color.WHITE);
            searchTextView.setThreshold(1);
            searchTextView.setAdapter(mAdapter);
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.red_cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background

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
                    if (getActivity() != null)
                        Util.hideKeyboard(getActivity(), searchTextView);
//                    MagazineFlipArticlesFragment fragment = (MagazineFlipArticlesFragment) getChildFragmentManager().getFragments().get(0);
                    List<String> tagIds = new ArrayList<String>();
                    tagIds.add(topicId);
//                    fragment.loadArticles(tagIds);
                    if (mMagazineFlipArticlesFragment != null) {
                        mMagazineFlipArticlesFragment.loadArticles(tagIds);
                    }

                    return;
                }

            });
            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    if (mMagazineFlipArticlesFragment != null) {
                        mMagazineFlipArticlesFragment.loadArticles(null);
//                        if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
//                            String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
//                            if (prefTags != null) {
//                                List<String> tagIds = Arrays.asList(prefTags);
//                                mMagazineFlipArticlesFragment.loadArticles(null);
//                            }
//                        }

                    }
                    return true;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
