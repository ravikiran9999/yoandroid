package com.yo.android.chat.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.chat.ui.fragments.GroupContactsFragment;
import com.yo.android.ui.BaseActivity;

public class CreateGroupActivity extends BaseActivity implements View.OnClickListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_chat_group);
        TextView addContactIcon = (TextView) findViewById(R.id.add_contact);
        addContactIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        GroupContactsFragment groupContactsFragment = new GroupContactsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, groupContactsFragment)
                .commit();
        enableBack();
    }
}
