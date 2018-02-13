package com.yo.android.chat.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.adapters.AppContactsListAdapter;
import com.yo.android.model.Registration;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MultipleContactsFragment extends Fragment implements AdapterView.OnItemLongClickListener{

    private ArrayList<Registration> arrayOfUsers;
    private AppContactsListAdapter appContactsListAdapter;
    private ListView listView;

    public MultipleContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //getRegisteredAppUsers();
        arrayOfUsers = new ArrayList<>();
        appContactsListAdapter = new AppContactsListAdapter(getActivity().getApplicationContext());
        listView.setAdapter(appContactsListAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multiple_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.lv_app_contacts);
        listView.setOnItemLongClickListener(this);
        return view;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }
}
