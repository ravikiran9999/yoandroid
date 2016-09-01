package com.yo.android.chat.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.yo.android.R;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.model.Contact;
import com.yo.android.model.Room;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.util.Constants;

import javax.inject.Inject;

/**
 * Created by Ramesh on 3/7/16.
 */
public class ChatActivity extends BaseActivity {

    private String opponent;
    private String opponentNumber;
    private String mOpponentImg;
    private Room room;

    @Inject
    ContactsSyncManager mContactsSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserChatFragment userChatFragment = new UserChatFragment();
        Bundle args = new Bundle();

        if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.ROOM)) {
            room = getIntent().getParcelableExtra(Constants.ROOM);

            args.putString(Constants.CHAT_ROOM_ID, room.getFirebaseRoomId());
            opponent = getOppenent(room);

            String opponentImg = room.getImage();
            if (opponentImg != null) {
                mOpponentImg = opponentImg;
                args.putString(Constants.OPPONENT_CONTACT_IMAGE, mOpponentImg);
            }

            if (opponent != null) {
                args.putString(Constants.OPPONENT_PHONE_NUMBER, opponent);
            }

            if (room.getGroupName() != null) {
                args.putString(Constants.TYPE, room.getGroupName());
            }

        } else if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.CONTACT)) {
            Contact contact = getIntent().getParcelableExtra(Constants.CONTACT);
            opponent = contact.getVoxUserName();
            args.putString(Constants.CHAT_ROOM_ID, contact.getFirebaseRoomId());
            args.putString(Constants.OPPONENT_PHONE_NUMBER, opponent);
            args.putString(Constants.OPPONENT_CONTACT_IMAGE, contact.getImage());
            args.putString(Constants.OPPONENT_ID, contact.getId());

        } else if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.YO_NOTIFICATION)) {
            opponent = getIntent().getStringExtra(Constants.VOX_USER_NAME);
            args.putString(Constants.CHAT_ROOM_ID, getIntent().getStringExtra(Constants.CHAT_ROOM_ID));
            args.putString(Constants.OPPONENT_PHONE_NUMBER, opponent);

        }
        if (getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD) != null) {
            args.putParcelableArrayList(Constants.CHAT_FORWARD, getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD));
        }
        userChatFragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, userChatFragment)
                .commit();
        enableBack();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            View customView = getLayoutInflater().inflate(R.layout.custom_title, null);

            TextView customTitle = (TextView) customView.findViewById(R.id.tv_phone_number);
            final ImageView imageView = (ImageView) customView.findViewById(R.id.imv_contact_pic);
            Contact contact = mContactsSyncManager.getContactByVoxUserName(opponent);
            if (contact != null && contact.getName() != null) {
                customTitle.setText(contact.getName());
            } else {
                customTitle.setText(opponent);
            }

            Glide.with(this).load(mOpponentImg)
                    .asBitmap().centerCrop()
                    .placeholder(R.drawable.ic_contactprofile)
                    .error(R.drawable.ic_contactprofile)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            imageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            customTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
                    intent.putExtra(Constants.OPPONENT_CONTACT_IMAGE, mOpponentImg);
                    intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponent);
                    intent.putExtra(Constants.FROM_CHAT_ROOMS, Constants.FROM_CHAT_ROOMS);
                    intent.putExtra(Constants.CHAT_ROOM_ID, room.getFirebaseRoomId());
                    intent.putExtra(Constants.GROUP_NAME, room.getGroupName());
                    startActivity(intent);
                }
            });
            getSupportActionBar().setCustomView(customView);
        }
    }

    private String getOppenent(@NonNull Room room) {

        if (room.getGroupName() == null) {

            return room.getVoxUserName();

        } else if (room.getGroupName() != null) {
            return room.getGroupName();
        }

        return null;
    }

}
