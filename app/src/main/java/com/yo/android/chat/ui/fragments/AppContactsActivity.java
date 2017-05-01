package com.yo.android.chat.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.yo.android.R;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;

import java.util.ArrayList;

public class AppContactsActivity extends BaseActivity {

    public static void start(Activity activity, ArrayList<ChatMessage> message) {
        Intent intent = createIntent(activity, message);
        activity.startActivity(intent);
        activity.finish();
    }

    private static Intent createIntent(Activity activity, ArrayList<ChatMessage> forward) {
        Intent intent = new Intent(activity, AppContactsActivity.class);
        intent.putExtra(Constants.IS_CHAT_FORWARD, true);
        if (forward != null && forward.size() > 0) {
            intent.putParcelableArrayListExtra(Constants.CHAT_FORWARD, forward);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        YoContactsFragment yoContactsFragment = new YoContactsFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.IS_CHAT_FORWARD, getIntent().hasExtra(Constants.IS_CHAT_FORWARD));
        if (getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD) != null) {
            args.putParcelableArrayList(Constants.CHAT_FORWARD, getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD));
        }
        yoContactsFragment.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, yoContactsFragment)
                .commit();
        enableBack();


    }
}
