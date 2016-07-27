package com.yo.android.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.model.Contact;
import com.yo.android.model.Room;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.util.Constants;

/**
 * Created by Ramesh on 3/7/16.
 */
public class ChatActivity extends BaseActivity {

    private Room room;
    private String opponent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserChatFragment userChatFragment = new UserChatFragment();
        Bundle args = new Bundle();

        if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.ROOM)) {
            room = getIntent().getParcelableExtra(Constants.ROOM);

        /*args.putString(Constants.CHAT_ROOM_ID, getIntent().getStringExtra(Constants.CHAT_ROOM_ID));
        args.putString(Constants.OPPONENT_PHONE_NUMBER, getIntent().getStringExtra(Constants.OPPONENT_PHONE_NUMBER));
        args.putString(Constants.OPPONENT_ID, getIntent().getStringExtra(Constants.OPPONENT_ID));*/

            args.putString(Constants.CHAT_ROOM_ID, room.getFirebaseRoomId());
            opponent = getOppenent(room);
            if (opponent != null) {

                args.putString(Constants.OPPONENT_PHONE_NUMBER, opponent);
            }

            if(room.getGroupName() != null) {
                args.putString(Constants.TYPE, room.getGroupName());
            }

            //args.putString(Constants.OPPONENT_ID, room.getId());

        } else if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.CONTACT)) {
            Contact contact = getIntent().getParcelableExtra(Constants.CONTACT);
            opponent = contact.getPhoneNo();
            args.putString(Constants.CHAT_ROOM_ID, contact.getFirebaseRoomId());
            args.putString(Constants.OPPONENT_PHONE_NUMBER, contact.getPhoneNo());
            args.putString(Constants.OPPONENT_ID, contact.getId());
        }

        //args.putString(Constants.OPPONENT_ID, getIntent().getStringExtra(Constants.OPPONENT_ID));
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

        //final String opponent = getIntent().getStringExtra(Constants.OPPONENT_PHONE_NUMBER);
        //final String opponent = getOppenent(room);

        if (getSupportActionBar() != null) {
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

    private String getOppenent(@NonNull Room room) {
        String opponent = null;
        String yourPhoneNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);

        if (room.getGroupName() == null) {
            if (!room.getMembers().get(0).getMobileNumber().equalsIgnoreCase(yourPhoneNumber)) {
                return room.getMembers().get(0).getMobileNumber();
            } else if (!room.getMembers().get(1).getMobileNumber().equalsIgnoreCase(yourPhoneNumber)) {
                return room.getMembers().get(1).getMobileNumber();
            }
        } else if (room.getGroupName() != null) {
            return room.getGroupName();
        }

        return opponent;
    }


}
