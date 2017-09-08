package com.yo.android.ui.fragments.findpeople;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.chat.ui.fragments.BaseFragment;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FollowersFragment extends BaseFragment {

    public static FollowersFragment newInstance() {
        Bundle args = new Bundle();
        //args.putSerializable(MyTripActivity.KEY_MYTRIP, myTrip);

        FollowersFragment fragment = new FollowersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public FollowersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_people, container, false);
        ButterKnife.bind(this, view);
        return null;
    }

}
