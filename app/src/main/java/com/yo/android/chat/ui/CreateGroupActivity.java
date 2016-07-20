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
import com.yo.android.util.Constants;

public class CreateGroupActivity extends BaseActivity implements View.OnClickListener{

    TextView groupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_chat_group);
        groupName = (TextView) findViewById(R.id.et_new_chat_group_name);
        TextView addContactIcon = (TextView) findViewById(R.id.add_contact);

        addContactIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(!groupName.getText().toString().equalsIgnoreCase("")) {

            String mGroupName = groupName.getText().toString();
            Bundle args = new Bundle();
            args.putString(Constants.GROUP_NAME,mGroupName);
            GroupContactsFragment groupContactsFragment = new GroupContactsFragment();
            groupContactsFragment.setArguments(args);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, groupContactsFragment)
                    .commit();
            enableBack();
        }
    }
}
