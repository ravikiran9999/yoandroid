package com.yo.android.ui.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by creatives on 6/27/2016.
 */
public class MagazinesFragment extends BaseFragment {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;

    private List<Topics> topicsList;

    private ArrayAdapter mAdapter;
    private Menu menu;
    private MagazineFlipArticlesFragment mMagazineFlipArticlesFragment;

    public MagazinesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        mMagazineFlipArticlesFragment = new MagazineFlipArticlesFragment();
        mAdapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());

        getChildFragmentManager().beginTransaction().add(R.id.bottom, mMagazineFlipArticlesFragment).commit();

        topicsList = new ArrayList<Topics>();

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        //showProgressDialog();
        yoService.tagsAPI(accessToken).enqueue(new Callback<List<Topics>>() {
            @Override
            public void onResponse(Call<List<Topics>> call, Response<List<Topics>> response) {
                dismissProgressDialog();
                if (response == null || response.body() == null) {
                    return;
                }
                topicsList.addAll(response.body());

                List<String> topicNamesList = new ArrayList<String>();
                for (int i = 0; i < topicsList.size(); i++) {
                    topicNamesList.add(topicsList.get(i).getName());
                }
                mAdapter.addAll(topicNamesList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Topics>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
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

            searchTextView.setTextColor(Color.BLACK);
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
                    String topicId = "";
                    for (int i = 0; i < topicsList.size(); i++) {
                        if (topicsList.get(i).getName().equals(topicName)) {
                            topicId = topicsList.get(i).getId();
                        }
                    }
                    MagazineFlipArticlesFragment fragment = (MagazineFlipArticlesFragment) getChildFragmentManager().getFragments().get(0);
                    List<String> tagIds = new ArrayList<String>();
                    tagIds.add(topicId);
                    fragment.loadArticles(tagIds);


                    return;
                }

            });
            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    if (mMagazineFlipArticlesFragment != null) {
                        mMagazineFlipArticlesFragment.loadAllArticles();
                    }
                    return true;
                }
            });

        } catch (Exception e) {
        }
    }

    public Menu getMenu() {
        return menu;
    }

}
