package com.yo.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yo.android.R;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.flip.MagazineTopicsSelectionFragment;
import com.yo.android.ui.NewMagazineActivity;

/**
 * Created by creatives on 6/27/2016.
 */
public class MagazinesFragment extends Fragment {

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
        getChildFragmentManager().beginTransaction().add(R.id.bottom, new MagazineFlipArticlesFragment()).commit();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()) {
            case R.id.menu_create_magazines:


                break;


        }
        return true;
    }

}
