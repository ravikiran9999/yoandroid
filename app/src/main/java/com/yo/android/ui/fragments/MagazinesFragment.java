package com.yo.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yo.android.R;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.flip.MagazineTopicsSelectionFragment;

/**
 * Created by creatives on 6/27/2016.
 */
public class MagazinesFragment extends Fragment {

    public MagazinesFragment() {
        // Required empty public constructor
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
        MagazineTopicsSelectionFragment fragment = new MagazineTopicsSelectionFragment();
        getChildFragmentManager().beginTransaction().add(R.id.top, fragment).commit();
        getChildFragmentManager().beginTransaction().add(R.id.bottom, new MagazineFlipArticlesFragment(fragment)).commit();
    }
}