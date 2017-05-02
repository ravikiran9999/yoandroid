package com.yo.android.chat.ui;

import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.yo.android.R;
import com.yo.android.api.YOUserInfo;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.notification.helper.NotificationCache;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.helpers.Settings;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.model.NotificationCountReset;
import com.yo.android.model.Room;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ramesh on 3/7/16.
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener{

    private final String TAG = ChatActivity.this.getClass().getSimpleName();
    private String opponent;
    private String mOpponentImg;
    private String title;
    private Room room;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private Contact contactfromOpponent;
    private Contact mContact;
    private String groupName;
    private String chatRoomId;
    @Bind(R.id.progress_layout)
    RelativeLayout progressLayout;

    @Inject
    ContactsSyncManager mContactsSyncManager;

    /**
     * navigated from YoContactsFragment
     *
     * @param activity
     * @param contact
     * @param forward
     */
    public static void start(Activity activity, Contact contact, ArrayList<ChatMessage> forward) {
        Intent intent = createIntent(activity, contact, forward);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * navigated from chatFragment
     *
     * @param activity
     * @param room
     */
    public static void start(Activity activity, Room room) {
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(Constants.ROOM, room);
        intent.putExtra(Constants.TYPE, Constants.ROOM);
        activity.startActivity(intent);
    }

    private static Intent createIntent(Activity activity, Contact contact, ArrayList<ChatMessage> forward) {
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(Constants.CONTACT, contact);
        intent.putExtra(Constants.TYPE, Constants.CONTACT);
        if (forward != null && forward.size() > 0) {
            intent.putParcelableArrayListExtra(Constants.CHAT_FORWARD, forward);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        ButterKnife.bind(this);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        //Clear all Notifications
        NotificationCache.clearNotifications();

        final UserChatFragment userChatFragment = new UserChatFragment();
        final Bundle args = new Bundle();

        if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.ROOM)) {
            room = getIntent().getParcelableExtra(Constants.ROOM);

            args.putString(Constants.CHAT_ROOM_ID, room.getFirebaseRoomId());
            opponent = getOpponent(room);

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
            callUserChat(args, userChatFragment);
        } else if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.CONTACT)) {
            final Contact contact = getIntent().getParcelableExtra(Constants.CONTACT);
            if (contact != null) {

                opponent = contact.getNexgieUserName();
                args.putString(Constants.CHAT_ROOM_ID, contact.getFirebaseRoomId());
                args.putString(Constants.OPPONENT_PHONE_NUMBER, opponent);
                args.putString(Constants.OPPONENT_CONTACT_IMAGE, contact.getImage());
                args.putString(Constants.OPPONENT_NAME, contact.getName());
                mContact = mContactsSyncManager.getContactByVoxUserName(opponent);
                if (mContact == null) {
                    //server request with opponent id
                    yoService.getYOUserInfoBYYOName(opponent).enqueue(new Callback<YOUserInfo>() {
                        @Override
                        public void onResponse(Call<YOUserInfo> call, Response<YOUserInfo> response) {
                            //update new data into database
                            contact.setId(response.body().getId());
                            args.putString(Constants.OPPONENT_ID, response.body().getId());
                            args.putParcelable(Constants.CONTACT, contact);
                            callUserChat(args, userChatFragment);
                        }

                        @Override
                        public void onFailure(Call<YOUserInfo> call, Throwable t) {
                            mToastFactory.showToast(R.id.chat_initiation_failed);
                            progressLayout.setVisibility(View.GONE);
                        }
                    });

                } else {
                    String contactId = contact.getId() == null ? mContact.getId() : contact.getId();
                    args.putString(Constants.OPPONENT_ID, contactId);
                    args.putParcelable(Constants.CONTACT, contact);
                    callUserChat(args, userChatFragment);
                }

                Util.cancelAllNotification(this);

            }

        } else if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.YO_NOTIFICATION)) {
            if (getIntent().hasExtra(Constants.OPPONENT_ID)) {
                args.putString(Constants.OPPONENT_ID, getIntent().getStringExtra(Constants.OPPONENT_ID));
            }

            if (getIntent().hasExtra(Constants.VOX_USER_NAME)) {

                opponent = getIntent().getStringExtra(Constants.VOX_USER_NAME);
                args.putString(Constants.OPPONENT_PHONE_NUMBER, opponent);
            }
            chatRoomId = getIntent().getStringExtra(Constants.CHAT_ROOM_ID);
            if (chatRoomId != null && !TextUtils.isEmpty(chatRoomId)) {
                args.putString(Constants.CHAT_ROOM_ID, chatRoomId);
            } else {
                Log.i(TAG, getString(R.string.chat_room_id_error));
            }

            if (getIntent().hasExtra(Constants.OPPONENT_PHONE_NUMBER)) {
                groupName = getIntent().getStringExtra(Constants.OPPONENT_PHONE_NUMBER);
                mOpponentImg = getIntent().getStringExtra(Constants.OPPONENT_CONTACT_IMAGE);
                args.putString(Constants.TYPE, groupName);
                args.putString(Constants.OPPONENT_CONTACT_IMAGE, mOpponentImg);
            }
            callUserChat(args, userChatFragment);
        }

        enableBack();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(true);

            View customView = getLayoutInflater().inflate(R.layout.custom_title, null);
            LinearLayout titleView = ButterKnife.findById(customView, R.id.title_view);
            TextView customTitle = ButterKnife.findById(customView, R.id.tv_phone_number);
            final ImageView imageView = ButterKnife.findById(customView, R.id.imv_contact_pic);

            if (mContact != null) {
                contactfromOpponent = mContact;
            } else if (groupName == null) {
                contactfromOpponent = mContactsSyncManager.getContactByVoxUserName(opponent);
            }

            if (contactfromOpponent != null && !TextUtils.isEmpty(contactfromOpponent.getName())) {
                title = contactfromOpponent.getName();
            } else if (room != null && !TextUtils.isEmpty(room.getFullName())) {
                title = room.getFullName();
            } else if (groupName != null) {
                title = groupName;
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
                if (!TextUtils.isEmpty(mOpponentImg)) {
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
                } else {
                    if (title != null && title.length() >= 1 && !TextUtils.isDigitsOnly(title)) {
                        if (Settings.isTitlePicEnabled) {
                            if (title != null && title.length() >= 1) {
                                Drawable drawable = Util.showFirstLetter(this, title);
                                imageView.setImageDrawable(drawable);
                            }
                        } else {
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.dynamic_profile));
                        }
                    } else {
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dynamic_profile));
                    }
                }
            }

            titleView.setOnClickListener(this);
            getSupportActionBar().setCustomView(customView);
        }
    }

    @Override
    public void onClick(View v) {
        String opponentTrim = null;
        if (opponent != null && opponent.contains(Constants.YO_USER)) {
            opponentTrim = opponent.replaceAll("[^\\d.]", "").substring(2, 12);
        }

        Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
        intent.putExtra(Constants.OPPONENT_CONTACT_IMAGE, mOpponentImg);
        String titles = title == null ? opponent : title;
        intent.putExtra(Constants.OPPONENT_NAME, titles);
        if (opponentTrim != null && TextUtils.isDigitsOnly(opponentTrim)) {
            intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentTrim);
        }

        intent.putExtra(Constants.FROM_CHAT_ROOMS, Constants.FROM_CHAT_ROOMS);

        if (groupName != null) {
            intent.putExtra(Constants.CHAT_ROOM_ID, chatRoomId);
            intent.putExtra(Constants.GROUP_NAME, title);
        } else {
            intent.putExtra(Constants.VOX_USER_NAME, opponent);
        }

        if (room != null) {
            intent.putExtra(Constants.CHAT_ROOM_ID, room.getFirebaseRoomId());
            intent.putExtra(Constants.GROUP_NAME, room.getGroupName());
        }
        startActivity(intent);
    }

    private void callUserChat(Bundle args, UserChatFragment userChatFragment) {
        try {
            if (getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD) != null) {
                args.putParcelableArrayList(Constants.CHAT_FORWARD, getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD));
            }
            userChatFragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, userChatFragment)
                    .commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getOpponent(@NonNull Room room) {

        if (room.getGroupName() == null) {
            if (!TextUtils.isEmpty(room.getNexgeUserName())) {
                return room.getNexgeUserName();
            } else if (!TextUtils.isEmpty(room.getMobileNumber())) {
                return room.getMobileNumber();
            }

        } else if (room.getGroupName() != null) {
            return room.getGroupName();
        }

        return null;
    }

    private void clearNotification(String opponent) {
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
            if (room != null) {
                shape.setColor(mColorGenerator.getColor(room.getFirebaseRoomId()));
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

    @Override
    protected void onPause() {
        super.onPause();
        progressLayout.setVisibility(View.GONE);
    }
}
