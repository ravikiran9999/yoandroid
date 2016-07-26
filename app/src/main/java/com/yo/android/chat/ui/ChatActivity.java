package com.yo.android.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.UserProfileActivity;
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
        args.putString(Constants.OPPONENT_PHONE_NUMBER, getIntent().getStringExtra(Constants.OPPONENT_PHONE_NUMBER));
        args.putString(Constants.OPPONENT_ID, getIntent().getStringExtra(Constants.OPPONENT_ID));

        //args.putString(Constants.YOUR_PHONE_NUMBER, getIntent().getStringExtra(Constants.YOUR_PHONE_NUMBER));
        if (getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD) != null) {
            args.putParcelableArrayList(Constants.CHAT_FORWARD, getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD));
        }
        userChatFragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, userChatFragment)
                .commit();
        enableBack();
        final String opponent = getIntent().getStringExtra(Constants.OPPONENT_PHONE_NUMBER);

        if (getSupportActionBar() != null) {
            //getSupportActionBar().setTitle(opponent);

            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            View customView = getLayoutInflater().inflate(R.layout.custom_title, null);

            TextView customTitle = (TextView) customView.findViewById(R.id.tv_phone_number);
            customTitle.setText(opponent);

            customTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);

                    intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponent);
                    startActivity(intent);
                }
            });
            getSupportActionBar().setCustomView(customView);
        }

    }
}
