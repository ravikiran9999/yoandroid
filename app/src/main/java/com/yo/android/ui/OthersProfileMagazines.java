package com.yo.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.CreateMagazinesAdapter;
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
    private CreateMagazinesAdapter adapter;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    @Inject
    YoApi.YoService yoService;
    private String userID;

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
        userID = getActivity().getIntent().getStringExtra(Constants.USER_ID);
        userID = "577a21902a8b0f000346d328";


        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getOtherProfilesMagazinesAPI(accessToken, userID).enqueue(new Callback<List<OwnMagazine>>() {
            @Override
            public void onResponse(Call<List<OwnMagazine>> call, Response<List<OwnMagazine>> response) {
                dismissProgressDialog();
                if (response.body().size() > 0) {
                    TextView count = (TextView) OthersProfileActivity.tabLayout.getTabAt(0).getCustomView().findViewById(R.id.count);
                    count.setText("" + response.body().size());
                    List<OwnMagazine> magazineList = response.body();
                    adapter = new CreateMagazinesAdapter(getActivity(), magazineList);
                    gridView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<OwnMagazine>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }
}
