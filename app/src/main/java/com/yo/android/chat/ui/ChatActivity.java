package com.yo.android.chat.ui;

import android.app.Activity;
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
import com.yo.android.chat.notification.localnotificationsbuilder.Notifications;
import com.yo.android.chat.ui.fragments.UserChatFragment;
import com.yo.android.helpers.Settings;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.model.GroupSubject;
import com.yo.android.model.NotificationCountReset;
import com.yo.android.model.Room;
import com.yo.android.model.Share;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.pjsip.SipBinder;
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

public class ChatActivity extends BaseActivity implements View.OnClickListener, UserChatFragment.UpdateStatus {

    private final String TAG = ChatActivity.this.getClass().getSimpleName();
    private String opponent;
    private String mOpponentImg;
    private String title;
    private Room room;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private Contact contactFromOpponent;
    private Contact mContact;
    private String groupName;
    private String chatRoomId;
    @Bind(R.id.progress_layout)
    RelativeLayout progressLayout;

    private SipBinder sipBinder;


    private TextView customTitle;
    public  TextView chatUserStatus;

    @Inject
    ContactsSyncManager mContactsSyncManager;

    public static void start(Activity activity, Contact contact, ArrayList<ChatMessage> forward) {
        Intent intent = createIntent(activity, contact, forward);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void start(Activity activity, Contact contact, Share share) {
        Intent intent = createIntent(activity, contact, share);
        activity.startActivity(intent);
        activity.finish();
    }

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
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (forward != null && forward.size() > 0) {
            intent.putParcelableArrayListExtra(Constants.CHAT_FORWARD, forward);
        }
        return intent;
    }

    private static Intent createIntent(Activity activity, Contact contact, Share share) {
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(Constants.CONTACT, contact);
        intent.putExtra(Constants.TYPE, Constants.CONTACT);
        intent.putExtra(Constants.CHAT_SHARE, share);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        final UserChatFragment userChatFragment = new UserChatFragment();
        final Bundle args = new Bundle();

        Util.cancelNotification(this, Notifications.CHAT_NOTIFICATION_ID);
        Util.cancelNotification(this, Notifications.GROUP_NOTIFICATION_ID);

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
            args.putString(Constants.FIREBASE_OPPONENT_USER_ID, room.getFirebaseUserId());
            callUserChat(args, userChatFragment);
        } else if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.CONTACT)) {
            final Contact contact = getIntent().getParcelableExtra(Constants.CONTACT);
            if (contact != null) {

                opponent = contact.getNexgieUserName();
                args.putString(Constants.OPPONENT_PHONE_NUMBER, opponent);

                mContact = mContactsSyncManager.getContactByVoxUserName(opponent);
                if (mContact == null) {
                    //server request with opponent id
                    yoService.getYOUserInfoBYYOName(opponent).enqueue(new Callback<YOUserInfo>() {
                        @Override
                        public void onResponse(Call<YOUserInfo> call, Response<YOUserInfo> response) {
                            //update new data into database
                            YOUserInfo yoUserInfo = response.body();
                            contact.setId(yoUserInfo.getId());
                            contact.setFirebaseRoomId(yoUserInfo.getFirebaseRoomId());
                            contact.setImage(yoUserInfo.getAvatar());
                            contact.setName(yoUserInfo.getFirst_name());

                            args.putString(Constants.CHAT_ROOM_ID, yoUserInfo.getFirebaseRoomId());
                            args.putString(Constants.OPPONENT_CONTACT_IMAGE, yoUserInfo.getAvatar());
                            args.putString(Constants.OPPONENT_ID, yoUserInfo.getId());
                            args.putParcelable(Constants.CONTACT, contact);
                            if (yoUserInfo.getFirebaseRoomId() != null && !TextUtils.isEmpty(yoUserInfo.getFirebaseRoomId())) {
                                callUserChat(args, userChatFragment);
                            } else {
                                mToastFactory.showToast(R.string.chat_room_id_error);
                            }
                            //callUserChat(args, userChatFragment);
                        }

                        @Override
                        public void onFailure(Call<YOUserInfo> call, Throwable t) {
                            mToastFactory.showToast(R.id.chat_initiation_failed);
                            progressLayout.setVisibility(View.GONE);
                        }
                    });

                } else {

                    String contactId = contact.getId() == null ? mContact.getId() : contact.getId();
                    String firebaseRoomId = contact.getFirebaseRoomId() == null ? mContact.getFirebaseRoomId() : contact.getFirebaseRoomId();
                    args.putString(Constants.OPPONENT_ID, contactId);
                    args.putString(Constants.FIREBASE_OPPONENT_USER_ID, contact.getFirebaseUserId());
                    args.putString(Constants.CHAT_ROOM_ID, firebaseRoomId);
                    args.putString(Constants.OPPONENT_CONTACT_IMAGE, contact.getImage());
                    //args.putParcelable(Constants.CONTACT, contact);
                    if (firebaseRoomId != null && !TextUtils.isEmpty(firebaseRoomId)) {
                        callUserChat(args, userChatFragment);
                    } else {
                        mToastFactory.showToast(R.string.chat_room_id_error);
                    }
                }
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
            if (chatRoomId != null && !TextUtils.isEmpty(chatRoomId)) {
                callUserChat(args, userChatFragment);
            } else {
                mToastFactory.showToast(R.string.chat_room_id_error);
            }
        }

        enableBack();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(true);

            View customView = getLayoutInflater().inflate(R.layout.custom_chat_title, null);
            LinearLayout titleView = ButterKnife.findById(customView, R.id.title_view);
            customTitle = ButterKnife.findById(customView, R.id.tv_phone_number);
            chatUserStatus = ButterKnife.findById(customView, R.id.tv_user_status);
            final ImageView imageView = ButterKnife.findById(customView, R.id.imv_contact_pic);

            if (mContact != null) {
                contactFromOpponent = mContact;
            } else if (groupName == null) {
                contactFromOpponent = mContactsSyncManager.getContactByVoxUserName(opponent);
            }

            if (contactFromOpponent != null && !TextUtils.isEmpty(contactFromOpponent.getName())) {
                title = contactFromOpponent.getName();
            } else if (room != null && !TextUtils.isEmpty(room.getFullName())) {
                title = room.getFullName();
            } else if (groupName != null) {
                title = groupName;
            } else if (opponent != null && opponent.contains(Constants.YO_USER)) {
                title = Util.numberFromNexgeFormat(opponent);
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
            opponentTrim = Util.numberFromNexgeFormat(opponent);
        }

        Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
        intent.putExtra(Constants.OPPONENT_CONTACT_IMAGE, mOpponentImg);
        String titles = title == null ? opponent : title;
        intent.putExtra(Constants.OPPONENT_NAME, titles);
        if (opponentTrim != null && TextUtils.isDigitsOnly(opponentTrim)) {
            String mOpponentNumber = String.format(getResources().getString(R.string.plus_number), opponentTrim);
            intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, mOpponentNumber);
        }

        if (groupName != null) {
            intent.putExtra(Constants.CHAT_ROOM_ID, chatRoomId);
            intent.putExtra(Constants.GROUP_NAME, title);
        } else if (room != null) {
            intent.putExtra(Constants.VOX_USER_NAME, opponent);
            intent.putExtra(Constants.CHAT_ROOM_ID, room.getFirebaseRoomId());
            intent.putExtra(Constants.GROUP_NAME, room.getGroupName());
        } else if (getIntent().getStringExtra(Constants.TYPE).equalsIgnoreCase(Constants.CONTACT)) {
            intent.putExtra(Constants.VOX_USER_NAME, contactFromOpponent.getNexgieUserName());
            intent.putExtra(Constants.CHAT_ROOM_ID, contactFromOpponent.getFirebaseRoomId());
        }

        intent.putExtra(Constants.FROM_CHAT_ROOMS, Constants.FROM_CHAT_ROOMS);

        startActivity(intent);
    }

    private void callUserChat(Bundle args, UserChatFragment userChatFragment) {
        try {
            Share share = getIntent().getParcelableExtra(Constants.CHAT_SHARE);
            if (getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD) != null) {
                args.putParcelableArrayList(Constants.CHAT_FORWARD, getIntent().getParcelableArrayListExtra(Constants.CHAT_FORWARD));
            } else if (share != null) {
                args.putParcelable(Constants.CHAT_SHARE, share);
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

        if (room.getGroupName() == null) { // group not exists
            if (!TextUtils.isEmpty(room.getNexgeUserName())) {
                return room.getNexgeUserName();
            } else if (!TextUtils.isEmpty(room.getMobileNumber())) {
                return room.getMobileNumber();
            }

        } else if (room.getGroupName() != null) { // group exists
            return room.getGroupName();
        }

        return null;
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
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressLayout.setVisibility(View.GONE);
    }

    public void onEventMainThread(GroupSubject groupSubject) {
        customTitle.setText(groupSubject.getUpdatedSubject());
    }

    @Override
    public void updateUserStatus(final boolean value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value) {
                    chatUserStatus.setText("online");
                } else {
                    chatUserStatus.setText("false");
                    chatUserStatus.setVisibility(View.GONE);
                }
            }
        });

    }
}
