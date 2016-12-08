package com.yo.android.chat.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
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
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
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
import com.yo.android.chat.firebase.Clipboard;
import com.yo.android.chat.firebase.ContactsSyncManager;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.helpers.Helper;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Contact;
import com.yo.android.model.Room;
import com.yo.android.pjsip.SipHelper;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.ShowPhotoActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import github.ankushsachdeva.emojicon.EmojiconGridView;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.emoji.Emojicon;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.yo.android.util.Util.copyFile;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserChatFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ChildEventListener, EmojiconsPopup.OnSoftKeyboardOpenCloseListener, EmojiconGridView.OnEmojiconClickedListener, TextWatcher {


    private static final String TAG = "UserChatFragment";

    private UserChatAdapter userChatAdapter;
    private ArrayList<ChatMessage> chatMessageArray;
    private HashMap<Integer, ArrayList<ChatMessage>> chatMessageHashMap;
    private EditText chatText;
    private StickyListHeadersListView listView;
    private String opponentNumber;
    private String opponentName;
    private String opponentId;
    private static File mFileTemp;
    private static final int ADD_IMAGE_CAPTURE = 1;
    private static final int ADD_SELECT_PICTURE = 2;
    private Uri mImageCaptureUri = null;
    private StorageReference storageReference;
    private Firebase authReference;
    private Firebase roomReference;
    private TextView listStickeyHeader;
    private int roomExist = 0;
    private Boolean isChildEventListenerAdd = Boolean.FALSE;
    private String childRoomId;
    List<ChatMessage> chatForwards;
    private int forwardInt = 0;
    private String mobilenumber;
    private String roomType;
    private EmojiconsPopup popup;
    private ImageView emoji;
    private ImageView cameraView;
    private int roomCreationProgress = 0;
    private String opponentImg;
    private Contact mContact;

    @Inject
    FireBaseHelper fireBaseHelper;

    @Inject
    ConnectivityHelper mHelper;

    @Inject
    ContactsSyncManager mContactsSyncManager;

    @Inject
    YoApi.YoService yoService;

    public UserChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        childRoomId = bundle.getString(Constants.CHAT_ROOM_ID);
        opponentNumber = bundle.getString(Constants.OPPONENT_PHONE_NUMBER);
        opponentId = bundle.getString(Constants.OPPONENT_ID);
        opponentImg = bundle.getString(Constants.OPPONENT_CONTACT_IMAGE);
        opponentName = bundle.getString(Constants.OPPONENT_NAME);
        mobilenumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl(BuildConfig.STORAGE_BUCKET);
        mContact = bundle.getParcelable(Constants.CONTACT);

        chatForwards = bundle.getParcelableArrayList(Constants.CHAT_FORWARD);
        mLog.e(TAG, "Firebase token reading from pref " + preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN));
        authReference = fireBaseHelper.authWithCustomToken(getActivity(), preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN));
        chatMessageArray = new ArrayList<>();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_chat, container, false);

        roomType = getArguments().getString(Constants.TYPE);

        listView = (StickyListHeadersListView) view.findViewById(R.id.listView);
        listStickeyHeader = (TextView) view.findViewById(R.id.time_stamp_header);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setOnItemClickListener(this);
        View send = view.findViewById(R.id.send);
        emoji = (ImageView) view.findViewById(R.id.emojiView);
        cameraView = (ImageView) view.findViewById(R.id.cameraView);
        chatText = (EditText) view.findViewById(R.id.chat_text);

        chatMessageHashMap = new HashMap<>();
        userChatAdapter = new UserChatAdapter(getActivity(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER), roomType, mContactsSyncManager);
        listView.setAdapter(userChatAdapter);
        listView.smoothScrollToPosition(userChatAdapter.getCount());
        listView.setVerticalScrollBarEnabled(true);
        listView.setClipToPadding(false);
        listView.setPadding(0, Helper.dp(getActivity(), 4), 0, Helper.dp(getActivity(), 3));
        listView.setLayoutAnimation(null);
        listView.setStackFromBottom(true);

        chatText.addTextChangedListener(this);
        listView.setOnItemClickListener(this);
        final View rootView = view.findViewById(R.id.root_view);
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
            registerChildEventListener(roomReference);

            if (chatForwards != null) {
                forwardInt = chatForwards.size() + 1;
                receiveForward(chatForwards);
            }
        }

        if ((childRoomId == null) && (chatForwards != null)) {
            createRoom("Message", null);
        }

        return view;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
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
                        if (!selectedItem.getSenderID().equals(mobilenumber)) {
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
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.delete:
                        SparseBooleanArray selected = userChatAdapter.getSelectedIds();
                        for (int i = selected.size() - 1; i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                final ChatMessage selectedItem = (ChatMessage) listView.getItemAtPosition(selected.keyAt(i));
                                roomReference.child(Long.toString(selectedItem.getTime())).removeValue();
                                userChatAdapter.removeItem(selectedItem);
                                chatMessageArray.remove(selectedItem);
                            }
                        }
                        mode.finish();
                        selected.clear();
                        break;
                    case R.id.copy:
                        StringBuilder builder = new StringBuilder();
                        selected = userChatAdapter.getSelectedIds();
                        if (selected.size() > 1) {
                            Toast.makeText(getActivity(), selected.size() + " " + getString(R.string.copy_messages), Toast.LENGTH_SHORT).show();
                        } else if (selected.size() == 1) {
                            Toast.makeText(getActivity(), getString(R.string.copy_message), Toast.LENGTH_SHORT).show();
                        }

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
                        forwardMessage(chatMessageArrayList);
                        mode.finish();
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
        switch (item.getItemId()) {
            case R.id.call:
                if (opponentNumber != null) {
                    SipHelper.makeCall(getActivity(), opponentNumber);
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

                //else, open the text keyboard first and immediately after that show the emoji popup
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
        }
    }

    private void viewContact() {
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentNumber);
        intent.putExtra(Constants.OPPONENT_CONTACT_IMAGE, opponentImg);
        intent.putExtra(Constants.OPPONENT_NAME, opponentName);
        intent.putExtra(Constants.FROM_CHAT_ROOMS, Constants.FROM_CHAT_ROOMS);

        if (roomType != null) {
            intent.putExtra(Constants.CHAT_ROOM_ID, childRoomId);
            intent.putExtra(Constants.GROUP_NAME, roomType);
        }

        startActivity(intent);
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
        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(type);
        chatMessage.setTime(timestamp);
        chatMessage.setSenderID(userId);
        chatMessage.setSent(0); // message sent 0, read 1
        chatMessage.setDelivered(0);
        chatMessage.setDeliveredTime(0);
        chatMessage.setRoomId(childRoomId);
        //chatMessage.setChatProfileUserName(preferenceEndPoint.getStringPreference(Constants.USER_NAME));
        chatMessage.setVoxUserName(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
        chatMessage.setYouserId(preferenceEndPoint.getStringPreference(Constants.USER_ID));
        chatMessage.setMsgID(message.hashCode());
        if (!TextUtils.isEmpty(roomType)) {
            chatMessage.setRoomName(roomType);
        }

        if (type.equals(Constants.TEXT)) {
            chatMessage.setMessage(message);
        } else if (type.equals(Constants.IMAGE)) {
            chatMessage.setImagePath(message);
        }

        if (roomExist == 0 && TextUtils.isEmpty(childRoomId)) {
            if (roomCreationProgress == 0) {
                roomCreationProgress = 1;
                createRoom(message, chatMessage);

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

                chatMessageArray.add(chatMessage);
                userChatAdapter.addItems(chatMessageArray);
                chatMessageHashMap.put(chatMessage.getMsgID(), chatMessageArray);

            }

            Map<String, Object> updateMessageMap = new ObjectMapper().convertValue(chatMessage, Map.class);
            final Firebase roomChildReference = roomReference.child(timeStp);
            roomChildReference.updateChildren(updateMessageMap, new Firebase.CompletionListener() {

                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    Log.w(TAG, "ONCOMPLETE");
                    if ((firebaseError != null) && (firebaseError.getCode() == -3)) {
                        Log.w(TAG, "ONCOMPLETE fail");

                        Activity activity = getActivity();
                        if (activity != null) {
                            Toast.makeText(activity, "Message not sent", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "ONCOMPLETE success");
                        chatMessage.setSent(1);
                        userChatAdapter.notifyDataSetChanged();
                        ;
                    }
                }
            });


        } catch (FirebaseException | NullPointerException e) {
            e.printStackTrace();
        }
    }


    private void registerChildEventListener(Firebase roomReference) {
        if (!isChildEventListenerAdd) {
            //isChildEventListenerAdd = Boolean.TRUE;
            roomReference.addChildEventListener(this);
            roomReference.keepSynced(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isChildEventListenerAdd) {
            roomReference.removeEventListener(this);
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
                    addSelectPicture(data);
                }

                break;
            default:
                break;
        }
    }

    private void addSelectPicture(Intent data) {
        Uri targetUri = data.getData();
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
                                String path = new CompressImage(getActivity()).compressImage(mFileTemp.getAbsolutePath());
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
            String path = new CompressImage(getActivity()).compressImage(mPartyPicUri);
            mFileTemp.delete();
            updateChatWithLocalImage(path);
        } catch (Exception e) {

        }
    }

    @NonNull
    private void updateChatWithLocalImage(String mPartyPicUri) {
        ChatMessage message = new ChatMessage();
        message.setType(Constants.IMAGE);
        message.setSenderID(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        message.setImagePath(mPartyPicUri);
        message.setTime(System.currentTimeMillis());
        userChatAdapter.UpdateItem(message);
        if (mPartyPicUri != null) {
            uploadImage(mPartyPicUri);
        }
    }

    /**
     * upload image to firebase storage
     *
     * @param path
     */
    private void uploadImage(final String path) {

        Uri file = Uri.fromFile(new File(path));

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        StorageReference imagesRef = storageReference.child("images/" + file.getLastPathSegment());
        UploadTask uploadTask = imagesRef.putFile(file, metadata);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle unsuccessful uploads
                uploadImage(path);
                e.printStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if (downloadUrl != null) {
                    sendImage(downloadUrl.getLastPathSegment());
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

    private void sendImage(@NonNull String imagePathName) {
        String userId = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        long timestamp = System.currentTimeMillis();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.IMAGE);
        chatMessage.setTime(timestamp);
        chatMessage.setImagePath(imagePathName);
        chatMessage.setSenderID(userId);
        sendChatMessage(chatMessage);
    }


    private void forwardMessage(ArrayList<ChatMessage> message) {
        Intent intent = new Intent(getActivity(), AppContactsActivity.class);
        intent.putParcelableArrayListExtra(Constants.CHAT_FORWARD, message);
        intent.putExtra(Constants.IS_CHAT_FORWARD, true);

        startActivity(intent);
        getActivity().finish();
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
                listView.smoothScrollToPosition(userChatAdapter.getCount() - 1);

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
                listView.smoothScrollToPosition(userChatAdapter.getCount());


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onChildRemoved(com.firebase.client.DataSnapshot dataSnapshot) {
        // this method will be triggered on child removed
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

    private void createRoom(final String message, final ChatMessage chatMessage) {
        String access = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        List<String> selectedUsers = new ArrayList<>();
        selectedUsers.add(opponentId);
        showProgressDialog();
        yoService.getRoomAPI(access, selectedUsers).enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                dismissProgressDialog();
                if (response.isSuccessful()) {
                    Room room = response.body();
                    if (room.getFirebaseRoomId() != null) {
                        roomExist = 1;
                        roomCreationProgress = 0;
                        roomReference = authReference.child(Constants.ROOMS).child(room.getFirebaseRoomId()).child(Constants.CHATS);
                        registerChildEventListener(roomReference);

                        if (chatForwards != null) {
                            receiveForward(chatForwards);
                        } else if (chatMessage != null) {
                            chatMessage.setRoomId(room.getFirebaseRoomId());
                            chatMessage.setVoxUserName(room.getVoxUserName());
                            sendChatMessage(chatMessage);
                        }
                        update(opponentNumber, room.getFirebaseRoomId());
                        EventBus.getDefault().post(Constants.CHAT_ROOM_REFRESH);
                    }
                } else {
                    if (chatText != null) {
                        //Restore the message if room fails
                        chatText.setText(message);
                        roomCreationProgress = 0;
                        roomExist = 0;
                        mToastFactory.showToast("Chat initiation failed! Please try again.");
                    }
                }
            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                dismissProgressDialog();
            }
        });
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
}

