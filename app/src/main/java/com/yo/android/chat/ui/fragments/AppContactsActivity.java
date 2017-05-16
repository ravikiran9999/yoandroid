package com.yo.android.chat.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import com.yo.android.model.ChatMessage;
import com.yo.android.model.Share;
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

        //get the received intent
        Intent receivedIntent = getIntent();
        //get the action
        String receivedAction = receivedIntent.getAction();
        //find out what we are dealing with
        String receivedType = receivedIntent.getType();

        YoContactsFragment yoContactsFragment = new YoContactsFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.IS_CHAT_FORWARD, getIntent().hasExtra(Constants.IS_CHAT_FORWARD));
        if (getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD) != null) {
            args.putParcelableArrayList(Constants.CHAT_FORWARD, getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD));
        } else if(receivedAction.equals(Intent.ACTION_SEND) && receivedType.startsWith("image")){
            Share share = new Share();
            Uri receivedUri = receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            share.setUri(receivedUri);
            share.setType(Constants.IMAGE);
            args.putParcelable(Constants.CHAT_SHARE, share);
        } else if(receivedAction.equals(Intent.ACTION_SEND) && receivedType.startsWith("text")) {
            Share share = new Share();
            String receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
            share.setText(receivedText);
            share.setType(Constants.TEXT);
            args.putParcelable(Constants.CHAT_SHARE, share);
        }
        yoContactsFragment.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, yoContactsFragment)
                .commit();
        enableBack();
    }
}
