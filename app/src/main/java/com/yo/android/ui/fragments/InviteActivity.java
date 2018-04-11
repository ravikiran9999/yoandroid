package com.yo.android.ui.fragments;

import android.app.Activity;
import android.os.Bundle;

import com.yo.android.R;
import com.yo.android.ui.BaseActivity;

public class InviteActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleHideIcon(R.string.invite_friends);
        InviteFriendsFragment inviteFriendsFragment = new InviteFriendsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, inviteFriendsFragment)
                .commit();
        enableBack();
    }
}
