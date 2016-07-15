package com.yo.android.ui.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.MoreListAdapter;
import com.yo.android.chat.ui.LoginActivity;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.MoreData;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends BaseFragment implements AdapterView.OnItemClickListener {


    private MoreListAdapter menuAdapter;
    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    FrameLayout changePhoto;

    public MoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.more_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        changePhoto = (FrameLayout) view.findViewById(R.id.change_layout);
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "You have selected change photo.", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareSettingsList();
    }

    /**
     * Prepares the Settings list
     */
    public void prepareSettingsList() {
        menuAdapter = new MoreListAdapter(getActivity()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_with_options;
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
    public List<MoreData> getMenuList() {
        String balance = preferenceEndPoint.getStringPreference(Constants.CURRENT_BALANCE, "2.0");

        List<MoreData> menuDataList = new ArrayList<>();
        menuDataList.add(new MoreData("John Doe", false));
        menuDataList.add(new MoreData("9987654321", false));
        menuDataList.add(new MoreData("Yo Credit " + "($" + balance + ")", true));
        menuDataList.add(new MoreData("Invite Friends", true));
        menuDataList.add(new MoreData("Notifications", true));
        menuDataList.add(new MoreData("Settings", true));
        menuDataList.add(new MoreData("Sign out", false));
        return menuDataList;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = ((MoreData) parent.getAdapter().getItem(position)).getName();

        if (name.equalsIgnoreCase("sign out")) {
            showLogoutDialog();
        } else {
            Toast.makeText(getActivity(), "You have clicked on " + name, Toast.LENGTH_LONG).show();
        }
    }

    public void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sign out");
        builder.setMessage("Are you sure you want to sign out ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preferenceEndPoint.clearAll();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();

            }
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }

}
