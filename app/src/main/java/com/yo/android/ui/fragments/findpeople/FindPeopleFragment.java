package com.yo.android.ui.fragments.findpeople;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.FindPeopleAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.FindPeople;
import com.yo.android.ui.OthersProfileActivity;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class FindPeopleFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    @Bind(R.id.lv_find_people)
    protected ListView lvFindPeople;
    @Bind(R.id.no_data)
    protected TextView noData;
    @Bind(R.id.ll_no_people)
    protected LinearLayout llNoPeople;
    @Bind(R.id.imv_empty_followings)
    protected ImageView imvEmptyFindPeople;
    @Bind(R.id.network_failure)
    protected TextView networkFailureText;
    @Bind(R.id.swipeContainer)
    protected SwipeRefreshLayout swipeRefreshContainer;

    @Inject
    YoApi.YoService yoService;

    private List<FindPeople> originalList;
    private FindPeopleAdapter findPeopleAdapter;
    private int pos;

    public static FindPeopleFragment newInstance() {
        Bundle args = new Bundle();
        //args.putSerializable(MyTripActivity.KEY_MYTRIP, myTrip);

        FindPeopleFragment fragment = new FindPeopleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public FindPeopleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        originalList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_people, container, false);
        ButterKnife.bind(this, view);

        findPeopleAdapter = new FindPeopleAdapter(getContext());
        lvFindPeople.setAdapter(findPeopleAdapter);
        //lvFindPeople.setOnScrollListener(onScrollListener());
        imvEmptyFindPeople.setImageResource(R.drawable.ic_empty_find_people);
        originalList = new ArrayList<>();
        callFindPeopleService(null);
        swipeRefreshContainer.setOnRefreshListener(this);

        lvFindPeople.setOnItemClickListener(this);

        return view;
    }

    /**
     * Calls the service to get the list of Yo app users
     * @param swipeRefreshContainer The SwipeRefreshLayout object
     */
    private void callFindPeopleService(final SwipeRefreshLayout swipeRefreshContainer) {
        if(swipeRefreshContainer != null) {
            swipeRefreshContainer.setRefreshing(false);
        } else {
            showProgressDialog();
        }
        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.getFindPeopleAPI(accessToken, 1, 30).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                try {
                    if (swipeRefreshContainer != null) {
                        swipeRefreshContainer.setRefreshing(false);
                    } else {
                        dismissProgressDialog();
                    }
                    if (response.body() != null && response.body().size() > 0) {
                        List<FindPeople> findPeopleList = response.body();
                        findPeopleAdapter.clearAll();
                        findPeopleAdapter.addItemsAll(findPeopleList);
                        lvFindPeople.setVisibility(View.VISIBLE);
                        noData.setVisibility(View.GONE);
                        llNoPeople.setVisibility(View.GONE);
                        originalList = response.body();
                        networkFailureText.setVisibility(View.GONE);

                    } else {
                        noData.setVisibility(View.GONE);
                        llNoPeople.setVisibility(View.VISIBLE);
                        lvFindPeople.setVisibility(View.GONE);
                        networkFailureText.setVisibility(View.GONE);
                    }
                } finally {
                    if(response != null && response.body() != null) {
                        try {
                            response.body().clear();
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                if(swipeRefreshContainer != null) {
                    swipeRefreshContainer.setRefreshing(false);
                } else {
                    dismissProgressDialog();
                }
                noData.setVisibility(View.GONE);
                llNoPeople.setVisibility(View.GONE);
                lvFindPeople.setVisibility(View.GONE);
                networkFailureText.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRefresh() {
        callFindPeopleService(swipeRefreshContainer);
    }

    /**
     * Refresh the Yo app users list
     */
    public void refresh() {
        callFindPeopleService(null);
        //pageCount = 1;
        findPeopleAdapter.clearAll();
        findPeopleAdapter.addItemsAll(originalList);
        lvFindPeople.setVisibility(View.VISIBLE);
        if(originalList.size()> 0) {
            noData.setVisibility(View.GONE);
            llNoPeople.setVisibility(View.GONE);
            networkFailureText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        pos = position;
        Intent otherProfileIntent = new Intent(getActivity(), OthersProfileActivity.class);
        otherProfileIntent.putExtra(Constants.USER_ID, findPeopleAdapter.getItem(position).getId());
        otherProfileIntent.putExtra("PersonName", findPeopleAdapter.getItem(position).getFirst_name() + " " + findPeopleAdapter.getItem(position).getLast_name());
        otherProfileIntent.putExtra("PersonPic", findPeopleAdapter.getItem(position).getAvatar());
        otherProfileIntent.putExtra("PersonIsFollowing", findPeopleAdapter.getItem(position).getIsFollowing());
        otherProfileIntent.putExtra("MagazinesCount", findPeopleAdapter.getItem(position).getMagzinesCount());
        otherProfileIntent.putExtra("FollowersCount", findPeopleAdapter.getItem(position).getFollowersCount());
        otherProfileIntent.putExtra("LikedArticlesCount", findPeopleAdapter.getItem(position).getLikedArticlesCount());
        startActivityForResult(otherProfileIntent, 8);
    }
}
