package com.yo.android.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yo.android.R;
import com.yo.android.adapters.MenuListAdapter;
import com.yo.android.model.MenuData;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends Fragment implements AdapterView.OnItemClickListener {


    private MenuListAdapter menuAdapter;

    public MoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_settings, container, false);
    }

    /**
     * Prepares the Settings list
     */
    public void prepareSettingsList() {
        menuAdapter = new MenuListAdapter(getActivity()) {
            @Override
            public int getLayoutId() {
                return R.layout.settings_row;
            }
        };
        ListView menuListView = (ListView) getView().findViewById(R.id.lv_settings);
        menuAdapter.addItems(getMenuList());

        menuListView.setAdapter(menuAdapter);
        menuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        menuListView.setSelection(0);
        menuListView.setOnItemClickListener(this);
    }

    /**
     * Creates the Settings list
     *
     * @return
     */
    public List<MenuData> getMenuList() {
        List<MenuData> menuDataList = new ArrayList<>();
        menuDataList.add(new MenuData("Credit/Wallet", R.drawable.ic_wallet));
        menuDataList.add(new MenuData("Invite Friends", R.drawable.ic_invitefriends));
        menuDataList.add(new MenuData("Notifications", R.drawable.ic_notifications));
        menuDataList.add(new MenuData("Profile", R.drawable.ic_profile));
        menuDataList.add(new MenuData("Settings", R.drawable.ic_settings));
        menuDataList.add(new MenuData("Logout", R.drawable.ic_logout));
        return menuDataList;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
