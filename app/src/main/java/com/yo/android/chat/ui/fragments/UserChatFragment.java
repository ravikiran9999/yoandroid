package com.yo.android.chat.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.adapters.UserChatAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.CompressImage;
import com.yo.android.chat.DeleteConfirmationListener;
import com.yo.android.chat.firebase.Clipboard;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.firebase.FirebaseService;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.Helper;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.RoomInfo;
import com.yo.android.model.Share;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.ShowPhotoActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.usecase.ChatNotificationUsecase;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.Util;
import com.yo.dialer.Dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.yo.android.util.Util.copyFile;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserChatFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ChildEventListener, EmojiconsPopup.OnSoftKeyboardOpenCloseListener, EmojiconGridView.OnEmojiconClickedListener, TextWatcher {


    private static final String TAG = "UserChatFragment";
    private static final String DummyMsgKey = "123456";

    private UserChatAdapter userChatAdapter;
    private ArrayList<ChatMessage> chatMessageArray;
    private HashMap<Integer, ArrayList<ChatMessage>> chatMessageHashMap;
    private String opponentNumber;
    private String sipAccountUserName;
    private String opponentName;
    private String opponentId;
    private String opponentFirebaseUserId;
    private static File mFileTemp;
    private static final int ADD_IMAGE_CAPTURE = 1;
    private static final int ADD_SELECT_PICTURE = 2;
    private Uri mImageCaptureUri = null;
    private StorageReference storageReference;
    private Firebase authReference;
    private Firebase roomReference;
    private Query messageQuery;
    private int roomExist = 0;
    private Boolean isChildEventListenerAdd = Boolean.FALSE;
    private String childRoomId;
    List<ChatMessage> chatForwards;
    private int forwardInt = 0;
    private String mobileNumber;
    private String roomType;
    private EmojiconsPopup popup;
    private int roomCreationProgress = 0;
    private String opponentImg;
    private int retryMessageCount = 0;
    private int falureCount = 0;
    private Share share;


    @Bind(R.id.emojiView)
    ImageView emoji;
    @Bind(R.id.cameraView)
    ImageView cameraView;
    @Bind(R.id.chat_text)
    EditText chatText;
    @Bind(R.id.listView)
    StickyListHeadersListView listView;
    @Bind(R.id.time_stamp_header)
    TextView listStickeyHeader;
    @Bind(R.id.send)
    View send;
    @Bind(R.id.root_view)
    View rootView;

    @Inject
    FireBaseHelper fireBaseHelper;

    @Inject
    ConnectivityHelper mHelper;

    @Inject
    ContactsSyncManager mContactsSyncManager;

    @Inject
    YoApi.YoService yoService;

    @Inject
    ChatNotificationUsecase chatNotificationUsecase;

    public interface UpdateStatus {
        void updateUserStatus(boolean value);
    }

    private UpdateStatus mUpdateStatus;

    public UserChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        childRoomId = bundle.getString(Constants.CHAT_ROOM_ID);
        opponentNumber = bundle.getString(Constants.OPPONENT_PHONE_NUMBER);
        sipAccountUserName = opponentNumber;
        opponentId = bundle.getString(Constants.OPPONENT_ID);
        opponentFirebaseUserId = bundle.getString(Constants.FIREBASE_OPPONENT_USER_ID);
        opponentImg = bundle.getString(Constants.OPPONENT_CONTACT_IMAGE);
        opponentName = bundle.getString(Constants.OPPONENT_NAME);
        mobileNumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl(BuildConfig.STORAGE_BUCKET);

        chatForwards = bundle.getParcelableArrayList(Constants.CHAT_FORWARD);
        share = bundle.getParcelable(Constants.CHAT_SHARE);
        mLog.e(TAG, "Firebase token reading from pref " + preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN));
        authReference = fireBaseHelper.authWithCustomToken(getActivity(), preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN), null);

        chatMessageArray = new ArrayList<>();
        setHasOptionsMenu(true);

        checkFirebaseUserStatus(authReference, opponentFirebaseUserId, mUpdateStatus);
        //checkFirebaseUserStatus(authReference, opponentFirebaseUserId);


        alarmManager();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mUpdateStatus = (UpdateStatus) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_chat, container, false);
        ButterKnife.bind(this, view);

        roomType = getArguments().getString(Constants.TYPE);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        //listView.setOnItemClickListener(this);
        chatMessageHashMap = new HashMap<>();
        userChatAdapter = new UserChatAdapter(getActivity(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER), roomType, mContactsSyncManager);
        listView.setAdapter(userChatAdapter);
        //listView.smoothScrollToPosition(userChatAdapter.getCount());
        setSmoothScrollPosition(userChatAdapter, listView);
        listView.setVerticalScrollBarEnabled(true);
        listView.setClipToPadding(false);
        listView.setPadding(0, Helper.dp(getActivity(), 4), 0, Helper.dp(getActivity(), 3));
        listView.setLayoutAnimation(null);
        listView.setStackFromBottom(true);

        chatText.addTextChangedListener(this);
        chatText.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        popup = new EmojiconsPopup(rootView, getActivity());
        send.setOnClickListener(this);
        popup.setSizeForSoftKeyboard();
        cameraView.setOnClickListener(this);
        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emoji, R.drawable.ic_emoji);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(this);

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(this);

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                chatText.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emoji.setOnClickListener(this);


        if (!TextUtils.isEmpty(childRoomId)) {
            roomExist = 1;

            roomReference = authReference.child(Constants.ROOMS).child(childRoomId).child(Constants.CHATS);
            messageQuery = roomReference.orderByValue().limitToLast(100); // show only last 100 items
            registerChildEventListener(messageQuery);

            if (chatForwards != null) {
                forwardInt = chatForwards.size() + 1;
                receiveForward(chatForwards);
            } else if (share != null && share.getType().equals(Constants.IMAGE)) {
                addSelectPicture(share.getUri());
            } else if (share != null && share.getType().equals(Constants.TEXT)) {
                sendChatMessage(share.getText(), share.getType());
            }
        }

        if ((childRoomId == null) && (chatForwards != null)) {
            //createRoom("Message", null);
        }


        return view;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            listView.setStackFromBottom(false);
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                                             @Override
                                             public void onScrollStateChanged(AbsListView view, int scrollState) {
                                                 if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                                                     listView.setAreHeadersSticky(false);
                                                 } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                                     listView.setAreHeadersSticky(true);
                                                 }
                                             }

                                             @Override
                                             public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                             }
                                         }

            );

        } catch (NoClassDefFoundError e) {
            mLog.w("UserChat", e);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

                ChatMessage chatMessage = (ChatMessage) listView.getItemAtPosition(position);
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(Integer.toString(checkedCount));

                userChatAdapter.toggleSelection(position);
                boolean imageSelected = false;
                SparseBooleanArray selected = userChatAdapter.getSelectedIds();
                boolean canDelete = true;

                for (int i = selected.size() - 1; i >= 0; i--) {
                    if (selected.valueAt(i)) {

                        final ChatMessage selectedItem = (ChatMessage) listView.getItemAtPosition(selected.keyAt(i));
                        if (selectedItem.getType().equalsIgnoreCase(Constants.IMAGE)) {
                            imageSelected = true;
                        }
                        if (!selectedItem.getSenderID().equals(mobileNumber)) {
                            canDelete = false;
                        }
                    }
                }

                Menu menu = mode.getMenu();
                menu.findItem(R.id.copy).setVisible(!imageSelected);
                menu.findItem(R.id.delete).setVisible(canDelete);

                if (chatMessage.getType().equalsIgnoreCase(Constants.IMAGE)) {
                    getActivity().invalidateOptionsMenu();
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_change, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                mode.getMenu().findItem(R.id.copy).setVisible(false);
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.delete:
                        if (Util.isOnline(getActivity())) {
                            Dialogs.chatDeleteConformation(getActivity(), new DeleteConfirmationListener() {

                                @Override
                                public void deleteProceed() {
                                    SparseBooleanArray selected = userChatAdapter.getSelectedIds();
                                    for (int i = selected.size() - 1; i >= 0; i--) {
                                        if (selected.valueAt(i)) {
                                            final ChatMessage selectedItem = (ChatMessage) listView.getItemAtPosition(selected.keyAt(i));
                                            roomReference.child(selectedItem.getMessageKey()).removeValue();
                                            userChatAdapter.removeItem(selectedItem);
                                            chatMessageArray.remove(selectedItem);
                                        }
                                    }
                                    mode.finish();
                                    selected.clear();
                                }

                                @Override
                                public void deleteCancle() {

                                }
                            }, userChatAdapter.getSelectedIds().size());
                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.delete_chat_without_network), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.copy:
                        StringBuilder builder = new StringBuilder();
                        SparseBooleanArray selected = userChatAdapter.getSelectedIds();

                        Toast.makeText(getActivity(), getResources().getQuantityString(R.plurals.copy_message, selected.size(), selected.size()), Toast.LENGTH_SHORT).show();

                        for (int i = 0; i < selected.size(); i++) {
                            if (selected.valueAt(i)) {
                                final ChatMessage selectedItem = (ChatMessage) listView.getItemAtPosition(selected.keyAt(i));
                                if (!selectedItem.getType().equals(getResources().getString(R.string.image))) {
                                    builder.append(selectedItem.getMessage());
                                    if (i < selected.size() - 1) {
                                        builder.append("\n");
                                    }
                                }
                            }
                        }
                        new Clipboard(getActivity()).copy(builder.toString());
                        userChatAdapter.getSelectedIds().clear();
                        userChatAdapter.notifyDataSetChanged();
                        mode.finish();
                        break;
                    case R.id.forward:
                        ArrayList<ChatMessage> chatMessageArrayList = new ArrayList<>();
                        selected = userChatAdapter.getSelectedIds();
                        for (int i = 0; i < selected.size(); i++) {
                            if (selected.valueAt(i)) {
                                final ChatMessage selectedItem = (ChatMessage) listView.getItemAtPosition(selected.keyAt(i));
                                chatMessageArrayList.add(selectedItem);
                            }
                        }
                        AppContactsActivity.start(getActivity(), chatMessageArrayList);
                        //mode.finish();
                        break;

                    default:
                        return false;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                userChatAdapter.getSelectedIds().clear();
            }
        });
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_user_chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!TextUtils.isEmpty(roomType)) {
            menu.findItem(R.id.call).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            Util.hideKeyboard(activity, chatText);
        }

        switch (item.getItemId()) {
            case R.id.call:
                if (sipAccountUserName != null) {
                    SipHelper.makeCall(activity, sipAccountUserName, false);
                }
                break;
            case R.id.camera:
                takePicture();
                break;
            case R.id.image:
                getImageFromGallery();
                break;
            case R.id.view_contact:
                viewContact();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send) {

            // Capture user id
            Map<String, String> chattingParams = new HashMap<String, String>();
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            //param keys and values have to be of String type
            chattingParams.put("UserId", userId);

            FlurryAgent.logEvent("Chatting", chattingParams);

            String message = chatText.getText().toString().trim();
            sendChatMessage(message, Constants.TEXT);
            if (chatText.getText() != null) {
                chatText.setText("");
            }

        } else if (v.getId() == R.id.emojiView) {

            //If popup is not showing => emoji keyboard is not visible, we need to show it
            if (!popup.isShowing()) {

                //If keyboard is visible, simply show the emoji popup
                if (popup.isKeyBoardOpen()) {
                    popup.showAtBottom();
                    changeEmojiKeyboardIcon(emoji, R.drawable.ic_action_keyboard);
                }

                //open the text keyboard first and immediately after that show the emoji popup
                else {
                    chatText.setFocusableInTouchMode(true);
                    chatText.requestFocus();
                    popup.showAtBottomPending();

                    final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(chatText, InputMethodManager.SHOW_IMPLICIT);
                    changeEmojiKeyboardIcon(emoji, R.drawable.ic_action_keyboard);
                }
            }

            //If popup is showing, simply dismiss it to show the undelying text keyboard
            else {
                popup.dismiss();
            }
        } else if (v.getId() == R.id.cameraView) {
            takePicture();
        } else if (v.getId() == R.id.chat_text) {
            setSmoothScrollPosition(userChatAdapter, listView);
        }
    }

    private void viewContact() {

        if (roomType != null) {
            UserProfileActivity.startGroup(getActivity(), childRoomId, roomType, opponentImg, opponentName, Constants.FROM_CHAT_ROOMS);
        } else if (opponentNumber != null && opponentNumber.contains(Constants.YO_USER)) {
            String opponentNumberTrim = Util.numberFromNexgeFormat(opponentNumber);
            UserProfileActivity.start(getActivity(), opponentNumberTrim, opponentNumber, opponentImg, opponentName, Constants.FROM_CHAT_ROOMS, null);
        }
    }

    private void sendChatMessage(String chatMessage, String type) {

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        String userId = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        if (chatMessage != null && !TextUtils.isEmpty(chatMessage.trim())) {
            sendChatMessage(chatMessage, userId, type);
        }
    }

    private void sendChatMessage(@NonNull final String message, @NonNull String userId, @NonNull String type) {
        long timestamp = System.currentTimeMillis();
        int msgId = (int) timestamp;
        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(type);
        chatMessage.setTime(timestamp);
        chatMessage.setSenderID(userId);
        chatMessage.setSent(0); // message sent 0, read 1
        chatMessage.setDelivered(0);
        chatMessage.setDeliveredTime(0);
        chatMessage.setVoxUserName(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
        chatMessage.setYouserId(preferenceEndPoint.getStringPreference(Constants.USER_ID));
        chatMessage.setMsgID(msgId);
        chatMessage.setMessageKey(DummyMsgKey);
        if (!TextUtils.isEmpty(roomType)) {
            chatMessage.setRoomName(roomType);
            chatMessage.setRoomImage(opponentImg);
        }

        if (type.equals(Constants.TEXT)) {
            chatMessage.setMessage(message);
        } else if (type.equals(Constants.IMAGE)) {
            chatMessage.setImagePath(message);
        }

        if (roomExist == 0 && TextUtils.isEmpty(childRoomId)) {
            if (roomCreationProgress == 0) {
                roomCreationProgress = 1;
                //createRoom(message, chatMessage);
            }
        } else {
            chatMessage.setRoomId(childRoomId);
            sendChatMessage(chatMessage);
        }
    }

    private void sendChatMessage(final ChatMessage chatMessage) {
        try {

            if (chatMessageArray == null) {
                chatMessageArray = new ArrayList<>();
            }

            String timeStp = Long.toString(chatMessage.getTime());
            if (!mHelper.isConnected()) {
                chatMessage.setSent(2);
            } else {
                chatMessage.setSent(1);
            }

            if (forwardInt == 0) {
                if (!chatMessageHashMap.keySet().contains(chatMessage.getMsgID())) {
                    chatMessageArray.add(chatMessage);
                    userChatAdapter.addItems(chatMessageArray);
                    //listView.smoothScrollToPosition(userChatAdapter.getCount() - 1);
                    setSmoothScrollPosition(userChatAdapter, listView);
                    chatMessageHashMap.put(chatMessage.getMsgID(), chatMessageArray);
                }
            }

            final Firebase roomInfoReference = roomReference.getParent().child(Constants.ROOM_INFO);
            roomInfoReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    RoomInfo roomInfo = dataSnapshot.getValue(RoomInfo.class);
                    if (roomInfo != null && roomInfo.getStatus().equalsIgnoreCase(Constants.ROOM_STATUS_INACTIVE)) {
                        roomInfo.setStatus(Constants.ROOM_STATUS_ACTIVE);
                        Map<String, Object> updateRoomStatusMap = new ObjectMapper().convertValue(roomInfo, Map.class);
                        roomInfoReference.updateChildren(updateRoomStatusMap);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    firebaseError.getMessage();

                }
            });

            Map<String, Object> updateMessageMap = new ObjectMapper().convertValue(chatMessage, Map.class);
            final Firebase roomChildReference = roomReference.push();
            roomChildReference.updateChildren(updateMessageMap, new Firebase.CompletionListener() {

                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    Activity activity = getActivity();
                    if ((firebaseError != null) && (firebaseError.getCode() == -3)) {
                        authReference = fireBaseHelper.authWithCustomToken(getActivity(), preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN), null);

                        if (retryMessageCount <= 3) {
                            sendChatMessage(chatMessage);
                            retryMessageCount++;

                        } else if (activity != null) {
                            Log.i(TAG, "firebaseToken :: " + preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN));
                            Log.i(TAG, "firebase User Id :: " + preferenceEndPoint.getStringPreference(Constants.FIREBASE_USER_ID));

                            Toast.makeText(activity, "Message not sent", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        chatMessage.setMessageKey(firebase.getKey());
                        chatMessage.setSent(1);
                        if (activity instanceof ChatActivity && !((ChatActivity) activity).chatUserStatus.getText().equals("online")) {
                            chatNotificationUsecase.pushChatMessage(chatMessage);
                        }
                        Map<String, Object> hashtaghMap = new ObjectMapper().convertValue(chatMessage, Map.class);
                        roomChildReference.updateChildren(hashtaghMap);
                        userChatAdapter.notifyDataSetChanged();
                    }
                }
            });


        } catch (FirebaseException | NullPointerException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void registerChildEventListener(Query chatRoomReference) {
        if (!isChildEventListenerAdd) {
            //isChildEventListenerAdd = Boolean.TRUE;
            chatRoomReference.addChildEventListener(this);
            chatRoomReference.keepSynced(true);
        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {

            String state = Environment.getExternalStorageState();
            String tempPhotoFileName = Long.toString(System.currentTimeMillis()) + ".jpg";
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File newFolder = new File(Environment.getExternalStorageDirectory() + "/YO/YOImages");
                if (!newFolder.exists()) {
                    newFolder.mkdirs();
                }
                mFileTemp = new File(newFolder.getAbsolutePath(), tempPhotoFileName);
            } else {
                mFileTemp = new File(getActivity().getFilesDir(), tempPhotoFileName);
            }
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mImageCaptureUri = Uri.fromFile(mFileTemp);
            }

            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, ADD_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            mLog.w(TAG, e);
        }
    }

    // open gallery
    private void getImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, ADD_SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {

            case Helper.CROP_ACTIVITY:
                if (data != null && data.hasExtra(Helper.IMAGE_PATH)) {
                    Uri imagePath = Uri.parse(data.getStringExtra(Helper.IMAGE_PATH));
                    updateChatWithLocalImage(imagePath.getPath());
                }
                break;
            case ADD_IMAGE_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    addImageCapture(data);
                }
                break;

            case ADD_SELECT_PICTURE:
                if (data != null) {
                    addSelectPicture(data.getData());
                }

                break;
            default:
                break;
        }
    }

    private void addSelectPicture(Uri targetUri) {
        //Uri targetUri = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        try {
            Cursor cursor = getActivity().getContentResolver().query(targetUri,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0]);
            final String filePath = cursor.getString(columnIndex);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    String state = Environment.getExternalStorageState();
                    String tempPhotoFileName = Long.toString(System.currentTimeMillis()) + ".jpg";
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        File newFolder = new File(Environment.getExternalStorageDirectory() + "/YO/YOImages");
                        if (!newFolder.exists()) {
                            newFolder.mkdirs();
                        }
                        mFileTemp = new File(newFolder.getAbsolutePath(), tempPhotoFileName);
                    } else {
                        mFileTemp = new File(getActivity().getFilesDir(), tempPhotoFileName);
                    }
                    copyFile(filePath, mFileTemp.getAbsolutePath());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mFileTemp != null && !Util.isKb(mFileTemp.length())) {
                                String path = new CompressImage(getActivity()).compressImage(mFileTemp.getAbsolutePath(), Constants.YOIMAGES);
                                mFileTemp.delete();
                                updateChatWithLocalImage(path);
                            } else {
                                updateChatWithLocalImage(mFileTemp.getAbsolutePath());
                            }

                        }
                    });
                }
            }).start();

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addImageCapture(Intent data) {
        try {
            String mPartyPicUri = mFileTemp.getPath();
            String path = new CompressImage(getActivity()).compressImage(mPartyPicUri, Constants.YOIMAGES);
            mFileTemp.delete();
            updateChatWithLocalImage(path);
        } catch (Exception e) {

        }
    }

    @NonNull
    private void updateChatWithLocalImage(String mPartyPicUri) {
        ChatMessage message = new ChatMessage();
        long timestamp = System.currentTimeMillis();
        int msgId = (int) timestamp;
        message.setType(Constants.IMAGE);
        message.setSenderID(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        message.setImagePath(mPartyPicUri);
        message.setTime(System.currentTimeMillis());
        message.setMsgID(msgId);


        if (mPartyPicUri != null) {
            uploadImage(message, mPartyPicUri);
        }

        if (!chatMessageHashMap.keySet().contains(message.getMsgID())) {
            chatMessageArray.add(message);
            userChatAdapter.addItems(chatMessageArray);
            chatMessageHashMap.put(message.getMsgID(), chatMessageArray);
        }

        // if network interruption is occurred
        if (!mHelper.isConnected()) {
            sendChatMessage(message);
        }
    }

    /**
     * upload image to firebase storage
     *
     * @param path
     */
    private void uploadImage(final ChatMessage imageMessage, final String path) {
        //private void uploadImage(final int messageId, final String path) {

        Uri file = Uri.fromFile(new File(path));

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        StorageReference imagesRef = storageReference.child("images/" + file.getLastPathSegment());
        final UploadTask uploadTask = imagesRef.putFile(file, metadata);

        uploadTask.addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
                falureCount++;
                if (falureCount <= 2) {
                    uploadImage(imageMessage, path);
                }
                e.printStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if (downloadUrl != null) {
                    if (!mHelper.isConnected()) {
                        updateImagePath(imageMessage, downloadUrl.getLastPathSegment());
                    } else {
                        sendImage(imageMessage, downloadUrl.getLastPathSegment());
                    }
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                Log.i(TAG, "Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(), "Upload is paused", Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * @param chatMessage
     * @param imagePathName update image path of existing image message
     */
    private void updateImagePath(@NonNull ChatMessage chatMessage, @NonNull String imagePathName) {

        String timeStamp = Long.toString(chatMessage.getTime());
        chatMessage.setImagePath(imagePathName);
        chatMessage.setDelivered(1);
        chatMessage.setDeliveredTime(System.currentTimeMillis());
        Map<String, Object> updateImagePathMap = new ObjectMapper().convertValue(chatMessage, Map.class);
        Firebase roomChildReference = roomReference.child(timeStamp);
        roomChildReference.updateChildren(updateImagePathMap);

    }

    private void sendImage(ChatMessage chatMessage, @NonNull String imagePathName) {

        String userId = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        long timestamp = System.currentTimeMillis();
        //int msgId = (int) timestamp;
        //ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.IMAGE);
        chatMessage.setTime(timestamp);
        chatMessage.setImagePath(imagePathName);
        chatMessage.setSenderID(userId);
        chatMessage.setMessageKey(DummyMsgKey);
        //chatMessage.setMsgID(msgId);
        chatMessage.setRoomId(childRoomId);
        chatMessage.setVoxUserName(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
        chatMessage.setYouserId(preferenceEndPoint.getStringPreference(Constants.USER_ID));
        if (!TextUtils.isEmpty(roomType)) {
            chatMessage.setRoomName(roomType);
        }
        sendChatMessage(chatMessage);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        userChatAdapter = (UserChatAdapter) listView.getAdapter();
        if (userChatAdapter.getItem(position).getType().equals(Constants.IMAGE)) {
            Intent photoIntent = new Intent(getActivity(), ShowPhotoActivity.class);
            photoIntent.putExtra(Constants.IMAGE, userChatAdapter.getItem(position).getImagePath());
            getActivity().startActivity(photoIntent);
        }
    }

    @Override
    public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
        try {

            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
            if (!chatMessageHashMap.keySet().contains(chatMessage.getMsgID())) {

                chatMessageArray.add(chatMessage);
                userChatAdapter.addItems(chatMessageArray);
                setSmoothScrollPosition(userChatAdapter, listView);


                if ((!chatMessage.getSenderID().equalsIgnoreCase(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) && (chatMessage.getDelivered() == 0) && getActivity() instanceof ChatActivity) {
                    long timestamp = System.currentTimeMillis();
                    chatMessage.setDelivered(1);
                    chatMessage.setDeliveredTime(timestamp);
                    Map<String, Object> hashtaghMap = new ObjectMapper().convertValue(chatMessage, Map.class);
                    dataSnapshot.getRef().updateChildren(hashtaghMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChildChanged(com.firebase.client.DataSnapshot dataSnapshot, String s) {
        ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
        if (getActivity() instanceof ChatActivity) {
            try {
                for (int i = 0; i < chatMessageArray.size(); i++) {
                    if (chatMessageArray.get(i).getTime() == chatMessage.getTime()) {
                        chatMessageArray.set(i, chatMessage);
                    }
                }
                userChatAdapter.addItems(chatMessageArray);
                setSmoothScrollPosition(userChatAdapter, listView);
                //listView.smoothScrollToPosition(userChatAdapter.getCount() - 1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onChildRemoved(com.firebase.client.DataSnapshot dataSnapshot) {
        // this method will be triggered on child removed
        try {
            ChatMessage removedChatMessage = dataSnapshot.getValue(ChatMessage.class);
            userChatAdapter.removeItem(removedChatMessage);
            chatMessageArray.remove(removedChatMessage);
            if (removedChatMessage.getType().equalsIgnoreCase(Constants.IMAGE)) {
                StorageReference imageReference = storageReference.child(removedChatMessage.getImagePath());
                imageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                        userChatAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                        e.getMessage();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChildMoved(com.firebase.client.DataSnapshot dataSnapshot, String s) {
        // this method will be triggered on child moved
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        firebaseError.getMessage();
    }

    private void receiveForward(List<ChatMessage> chatForwards) {

        if (chatForwards != null) {

            for (int i = 0; i < chatForwards.size(); i++) {
                forwardInt--;
                if (chatForwards.get(i).getType().equals(Constants.IMAGE)) {
                    sendChatMessage(chatForwards.get(i).getImagePath(), chatForwards.get(i).getType());
                } else if (chatForwards.get(i).getType().equals(Constants.TEXT)) {
                    sendChatMessage(chatForwards.get(i).getMessage(), chatForwards.get(i).getType());
                }
                chatForwards.get(i).setSelected(false);
            }
        }
        forwardInt = 0;
    }

    public void update(String voxUsername, String roomId) {
        Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI;
        String where = YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_PHONE_NUMBER + "=?";
        ContentValues contentValues = new ContentValues();
        contentValues.put(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_ROOM_ID, roomId);
        if (getActivity() != null) {
            getActivity().getContentResolver()
                    .update(uri, contentValues, where,
                            new String[]{voxUsername});
        }
    }

    @Override
    public void onKeyboardOpen(int keyBoardHeight) {
        // method will be called on keyboard open
    }

    @Override
    public void onKeyboardClose() {
        if (popup.isShowing())
            popup.dismiss();

    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        if (chatText == null || emojicon == null) {
            return;
        }

        int start = chatText.getSelectionStart();
        int end = chatText.getSelectionEnd();
        if (start < 0) {
            chatText.append(emojicon.getEmoji());
        } else {
            chatText.getText().replace(Math.min(start, end),
                    Math.max(start, end), emojicon.getEmoji(), 0,
                    emojicon.getEmoji().length());
        }
    }

    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // before text changed
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (count > 0 && !TextUtils.isEmpty(s.toString().trim())) {
            cameraView.setVisibility(View.GONE);
        } else if (count == 0 || TextUtils.isEmpty(s.toString().trim())) {
            cameraView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        // after text changed
    }

    public void onEventMainThread(String action) {
        if (action.equalsIgnoreCase(Constants.CHAT_MESSAGE_NOTIFICATION)) {
            registerChildEventListener(messageQuery);
        }
    }

    private void alarmManager() {
        Intent alarmIntent = new Intent(getActivity().getApplicationContext(), FirebaseService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        int interval = 2 * 60 * 1000; // 2 minutes interval

        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }

    private void setSmoothScrollPosition(UserChatAdapter chatAdapter, StickyListHeadersListView listView) {
        listView.setVerticalScrollBarEnabled(false);
        int count = chatAdapter.getCount() - 1;
        //Log.i(TAG, String.valueOf(count));
        listView.setSelection(count);
    }
}