package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.CreateMagazinesAdapter;
import com.yo.android.adapters.OthersMagazinesAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.OwnMagazine;
import com.yo.android.util.Constants;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by root on 15/7/16.
 */
public class OthersProfileMagazines extends BaseFragment {
    private GridView gridView;
    private OthersMagazinesAdapter adapter;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    @Inject
    YoApi.YoService yoService;
    private String userID;
    private TextView noData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.profile_magazines, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridView = (GridView) view.findViewById(R.id.magazines_gridview);
        noData = (TextView) view.findViewById(R.id.no_data);
        userID = getActivity().getIntent().getStringExtra(Constants.USER_ID);
        //userID = "577a21902a8b0f000346d328"


        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getOtherProfilesMagazinesAPI(accessToken, userID).enqueue(new Callback<List<OwnMagazine>>() {
            @Override
            public void onResponse(Call<List<OwnMagazine>> call, Response<List<OwnMagazine>> response) {
                dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    TextView count = (TextView) OthersProfileActivity.tabLayout.getTabAt(0).getCustomView().findViewById(R.id.count);
                    count.setText("" + response.body().size());
                    List<OwnMagazine> magazineList = response.body();
                    adapter = new OthersMagazinesAdapter(getActivity());
                    adapter.addItems(magazineList);
                    gridView.setAdapter(adapter);
                } else {
                    gridView.setVisibility(View.GONE);
                    noData.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<OwnMagazine>> call, Throwable t) {
                dismissProgressDialog();
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), OthersMagazinesDetailActivity.class);
                intent.putExtra("MagazineTitle", adapter.getItem(position).getName());
                intent.putExtra("MagazineId", adapter.getItem(position).getId());
                intent.putExtra("MagazineDesc", adapter.getItem(position).getDescription());
                intent.putExtra("MagazinePrivacy", adapter.getItem(position).getPrivacy());
                intent.putExtra("MagazineIsFollowing", adapter.getItem(position).getIsFollowing());
                startActivity(intent);
            }

            });
    }
}
