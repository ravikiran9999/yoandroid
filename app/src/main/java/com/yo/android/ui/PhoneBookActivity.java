package com.yo.android.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.yo.android.R;
import com.yo.android.chat.ui.fragments.ContactsFragment;
import com.yo.android.ui.BaseActivity;
import com.yo.android.util.Constants;

/**
 * Created by rajesh on 2/9/16.
 */
public class PhoneBookActivity extends BaseActivity {
    private static final String TAG = PhoneBookActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadfragmemnt);
        if (getIntent().hasExtra(Constants.FROM)) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle(R.string.contact);

        FrameLayout frame = (FrameLayout) findViewById(R.id.loadfragments);
        if (savedInstanceState == null) {
            ContactsFragment mFragment = new ContactsFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.loadfragments, mFragment, TAG)
                    .disallowAddToBackStack()
                    .commit();
        }

    }
}
