package com.yo.android.ui;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.yo.android.R;
import com.yo.android.chat.ui.fragments.ChatFragment;
import com.yo.android.chat.ui.fragments.ContactsFragment;

/**
 * Created by rajesh on 2/9/16.
 */
public class PhoneChatActivity extends BaseActivity {
    private static final String TAG = PhoneChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadfragmemnt);
        FrameLayout frame = (FrameLayout) findViewById(R.id.loadfragments);
        getSupportActionBar().setTitle(R.string.chats);

        if (savedInstanceState == null) {
            ChatFragment mFragment = new ChatFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.loadfragments, mFragment, TAG)
                    .disallowAddToBackStack()
                    .commit();
        }

    }
}
