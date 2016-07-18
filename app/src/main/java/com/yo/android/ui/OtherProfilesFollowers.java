package com.yo.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
    private int pageCount = 1;
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
        lvFindPeople.setOnScrollListener(onScrollListener());
        userID = getActivity().getIntent().getStringExtra(Constants.USER_ID);
        //userID = "577a21902a8b0f000346d328"
        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getOtherProfilesFollowersAPI(accessToken, userID).enqueue(new Callback<List<FindPeople>>() {
            @Override
            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                dismissProgressDialog();
                if (response.body().size() > 0) {
                    noData.setVisibility(View.GONE);
                    lvFindPeople.setVisibility(View.VISIBLE);
                    TextView count = (TextView) OthersProfileActivity.tabLayout.getTabAt(1).getCustomView().findViewById(R.id.count);
                    count.setText("" + response.body().size());
                    List<FindPeople> findPeopleList = response.body();
                    findPeopleAdapter.addItemsAll(findPeopleList);
                }
            }

            @Override
            public void onFailure(Call<List<FindPeople>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

    private AbsListView.OnScrollListener onScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int threshold = 1;
                int count = lvFindPeople.getCount();
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (lvFindPeople.getLastVisiblePosition() >= count - threshold) {
                        pageCount++;
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.getFindPeopleAPI(accessToken, pageCount, 30).enqueue(new Callback<List<FindPeople>>() {
                            @Override
                            public void onResponse(Call<List<FindPeople>> call, Response<List<FindPeople>> response) {
                                dismissProgressDialog();
                                if (response.body().size() > 0) {
                                    List<FindPeople> findPeopleList = response.body();
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

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
            }
        };
    }
}
