package com.yo.android.chat.ui;

import android.os.Bundle;

import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;

/**
 * Created by Ramesh on 3/7/16.
 */
public class ChatActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserChatFragment userChatFragment = new UserChatFragment();
        Bundle args = new Bundle();
        args.putString(Constants.CHAT_ROOM_ID, getIntent().getStringExtra(Constants.CHAT_ROOM_ID));
        userChatFragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, userChatFragment)
                .commit();
        enableBack();
        String opponent = getIntent().getStringExtra(Constants.OPPONENT_PHONE_NUMBER);
        getSupportActionBar().setTitle(opponent);
    }
}
