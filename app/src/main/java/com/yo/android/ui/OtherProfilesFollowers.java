package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.FindPeopleAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.FindPeople;
import com.yo.android.util.Constants;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by root on 15/7/16.
 */
public class OtherProfilesFollowers extends BaseFragment {
    private FindPeopleAdapter findPeopleAdapter;
    private ListView lvFindPeople;
    @Inject
    YoApi.YoService yoService;
    private String userID;
    private TextView noData;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.activity_find_people, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPeopleAdapter = new FindPeopleAdapter(getActivity());
        lvFindPeople = (ListView) view.findViewById(R.id.lv_find_people);
        noData = (TextView) view.findViewById(R.id.no_data);
        lvFindPeople.setAdapter(findPeopleAdapter);
        userID = getActivity().getIntent().getStringExtra(Constants.USER_ID);
        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        callService(accessToken);

        lvFindPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent otherProfileIntent = new Intent(getActivity(), OthersProfileActivity.class);
                otherProfileIntent.putExtra(Constants.USER_ID, findPeopleAdapter.getItem(position).getId());
                otherProfileIntent.putExtra("PersonName", findPeopleAdapter.getItem(position).getFirst_name() + " " + findPeopleAdapter.getItem(position).getLast_name());
                otherProfileIntent.putExtra("PersonPic", findPeopleAdapter.getItem(position).getAvatar());
                otherProfileIntent.putExtra("PersonIsFollowing", findPeopleAdapter.getItem(position).getIsFollowing());
                otherProfileIntent.putExtra("MagazinesCount", findPeopleAdapter.getItem(position).getMagzinesCount());
                otherProfileIntent.putExtra("FollowersCount", findPeopleAdapter.getItem(position).getFollowersCount());
                otherProfileIntent.putExtra("LikedArticlesCount", findPeopleAdapter.getItem(position).getLikedArticlesCount());
                startActivityForResult(otherProfileIntent, 11);
            }
        });
    }

    /**
     * Gets the list of other Yo app user's followers
     * @param accessToken
     */
    private void callService(String accessToken) {
        yoService.getOtherProfilesFollowersAPI(accessToken, userID).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body() != null) {
                    noData.setVisibility(View.GONE);
                    lvFindPeople.setVisibility(View.VISIBLE);
                    List<FindPeople> findPeopleList = response.body();
                    findPeopleAdapter.clearAll();
                    findPeopleAdapter.addItemsAll(findPeopleList);
                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
                noData.setVisibility(View.VISIBLE);
                lvFindPeople.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Used to call the service to get the list of other Yo app user's followers
     */
    public void update(){
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        callService(accessToken);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11 && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                showProgressDialog();
                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                yoService.getOtherProfilesFollowersAPI(accessToken, userID).enqueue(new Callback<List<FindPeople>>() {
                    @Override
                    public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                        dismissProgressDialog();
                        if (response.body().size() > 0) {
                            noData.setVisibility(View.GONE);
                            lvFindPeople.setVisibility(View.VISIBLE);
                            List<FindPeople> findPeopleList = response.body();
                            findPeopleAdapter.clearAll();
                            findPeopleAdapter.addItemsAll(findPeopleList);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                        dismissProgressDialog();
                    }
                });
            }

        }
    }

}
