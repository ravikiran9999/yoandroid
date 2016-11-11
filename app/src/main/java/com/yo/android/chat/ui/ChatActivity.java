package com.yo.android.chat.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.yo.android.R;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.notification.helper.NotificationCache;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.model.NotificationCountReset;
import com.yo.android.model.Room;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by Ramesh on 3/7/16.
 */
public class ChatActivity extends BaseActivity {

    private String opponent;
    private String mOpponentImg;
    private String title;
    private Room room;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private Contact nContact = null;
    private Contact contactfromOpponent;
    @Inject
    ContactsSyncManager mContactsSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        //Clear all Notifications
        NotificationCache.clearNotifications();


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

            if (room.getFullName() != null) {
                args.putString(Constants.OPPONENT_NAME, room.getFullName());
            }

            if (room.getGroupName() != null) {
                args.putString(Constants.TYPE, room.getGroupName());
            }

            args.putString(Constants.OPPONENT_ID, room.getYouserId());

            Util.cancelAllNotification(this);

        } else if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.CONTACT)) {
            String mContactId = null;
            Contact contact = getIntent().getParcelableExtra(Constants.CONTACT);
            if (contact != null) {
                opponent = contact.getVoxUserName();
                args.putString(Constants.CHAT_ROOM_ID, contact.getFirebaseRoomId());
                args.putString(Constants.OPPONENT_PHONE_NUMBER, opponent);
                args.putString(Constants.OPPONENT_CONTACT_IMAGE, contact.getImage());
                Contact mContact = mContactsSyncManager.getContactByVoxUserName(opponent);
                if (contact.getId() == null && mContact != null) {
                    nContact = mContactsSyncManager.getContactByVoxUserName(opponent);
                    //mContactId = mContactsSyncManager.getContactByVoxUserName(opponent).getId();
                }

                String contactId = contact.getId() == null ? nContact.getId() : contact.getId();
                args.putString(Constants.OPPONENT_ID, contactId);

                Util.cancelAllNotification(this);

            }

        } else if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.YO_NOTIFICATION)) {
            if (getIntent().hasExtra(Constants.OPPONENT_ID)) {
                args.putString(Constants.OPPONENT_ID, getIntent().getStringExtra(Constants.OPPONENT_ID));
            }

            if (getIntent().hasExtra(Constants.VOX_USER_NAME)) {

                opponent = getIntent().getStringExtra(Constants.VOX_USER_NAME);
                args.putString(Constants.CHAT_ROOM_ID, getIntent().getStringExtra(Constants.CHAT_ROOM_ID));
                args.putString(Constants.OPPONENT_PHONE_NUMBER, opponent);
            }

            if (getIntent().getStringExtra(Constants.OPPONENT_PHONE_NUMBER) != null) {
                args.putString(Constants.TYPE, getIntent().getStringExtra(Constants.OPPONENT_PHONE_NUMBER));
            }
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
            LinearLayout titleView = (LinearLayout) customView.findViewById(R.id.title_view);
            TextView customTitle = (TextView) customView.findViewById(R.id.tv_phone_number);
            final ImageView imageView = (ImageView) customView.findViewById(R.id.imv_contact_pic);

            if (nContact != null) {
                contactfromOpponent = nContact;
            } else {
                contactfromOpponent = mContactsSyncManager.getContactByVoxUserName(opponent);
            }

            if (contactfromOpponent != null && !TextUtils.isEmpty(contactfromOpponent.getName())) {
                title = contactfromOpponent.getName();
            } else if (room != null && !TextUtils.isEmpty(room.getFullName())) {
                title = room.getFullName();
            } else if (opponent != null && opponent.contains(Constants.YO_USER)) {
                title = opponent.replaceAll("[^\\d.]", "").substring(2, 12);
            }
            if (title != null) {
                customTitle.setText(title);
            } else {
                customTitle.setText(opponent);
            }

            if (room != null && room.getGroupName() != null) {
                Glide.with(this).load(mOpponentImg)
                        .asBitmap().centerCrop()
                        .placeholder(loadAvatarImage(imageView, true))
                        .error(loadAvatarImage(imageView, true))
                        .dontAnimate()
                        .into(new BitmapImageViewTarget(imageView) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                if (imageView.getTag(Settings.imageTag) != null) {
                                    imageView.setTag(Settings.imageTag, circularBitmapDrawable);
                                }
                                imageView.setImageDrawable((Drawable) imageView.getTag(Settings.imageTag));
                            }
                        });
            } else {
                Glide.with(this).load(mOpponentImg)
                        .asBitmap().centerCrop()
                        .dontAnimate()
                        .placeholder(loadAvatarImage(imageView, false))
                        .error(loadAvatarImage(imageView, false))
                        .into(new BitmapImageViewTarget(imageView) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                if (imageView.getTag(Settings.imageTag) != null) {
                                    imageView.setTag(Settings.imageTag, circularBitmapDrawable);
                                }
                                imageView.setImageDrawable((Drawable) imageView.getTag(Settings.imageTag));
                            }
                        });
            }

            titleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (opponent != null && opponent.contains(Constants.YO_USER)) {
                        opponent = opponent.replaceAll("[^\\d.]", "").substring(2, 12);
                    }

                    Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
                    intent.putExtra(Constants.OPPONENT_CONTACT_IMAGE, mOpponentImg);
                    String titles = title == null ? opponent : title;
                    intent.putExtra(Constants.OPPONENT_NAME, titles);
                    intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponent);
                    intent.putExtra(Constants.FROM_CHAT_ROOMS, Constants.FROM_CHAT_ROOMS);

                    if (room != null) {
                        intent.putExtra(Constants.CHAT_ROOM_ID, room.getFirebaseRoomId());
                        intent.putExtra(Constants.GROUP_NAME, room.getGroupName());
                    }
                    startActivity(intent);
                }
            });
            getSupportActionBar().setCustomView(customView);
        }
    }

    private String getOppenent(@NonNull Room room) {

        if (room.getGroupName() == null) {
            if (!TextUtils.isEmpty(room.getVoxUserName())) {
                return room.getVoxUserName();
            } else if (!TextUtils.isEmpty(room.getMobileNumber())) {
                return room.getMobileNumber();
            }

        } else if (room.getGroupName() != null) {
            return room.getGroupName();
        }

        return null;
    }

    private void clearNotification(String opponent) {

        //long opp = Long.parseLong(opponent.replaceAll("[^\\d.]", "").substring(2, 12));
        //int opponentInt = (int) opp;
        //Util.cancelReadNotification(this, opponentInt);
        Util.cancelAllNotification(this);
    }

    private Drawable loadAvatarImage(ImageView imageview, boolean isgroup) {
        if (imageview.getTag() != null) {
            return (Drawable) imageview.getTag(Settings.imageTag);
        }

        Drawable tempImage;
        if (isgroup) {
            tempImage = getResources().getDrawable(R.drawable.chat_group);
        } else {
            tempImage = getResources().getDrawable(R.drawable.dynamic_profile);
        }
        if (!Settings.isTitlePicEnabled) {
            return tempImage;
        }
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            int existingColor = mColorGenerator.getColor(shape);
            if (existingColor == 0) {
                shape.setColor(mColorGenerator.getRandomColor());
            } else {
                shape.setColor(existingColor);
            }
        }
        imageview.setTag(Settings.imageTag, tempImage);
        return tempImage;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new NotificationCountReset(0));
    }
}
