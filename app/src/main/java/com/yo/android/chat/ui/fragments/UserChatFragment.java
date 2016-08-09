package com.yo.android.chat.ui.fragments;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.adapters.UserChatAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.firebase.Clipboard;
import com.yo.android.chat.firebase.FirebaseService;
import com.yo.android.chat.firebase.MyServiceConnection;
import com.yo.android.chat.ui.ChatActivity;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.Room;
import com.yo.android.provider.YoAppContactContract;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.ShowPhotoActivity;
import com.yo.android.ui.UserProfileActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.FireBaseHelper;
import com.yo.android.voip.OutGoingCallActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserChatFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ChildEventListener {


    private static final String TAG = "UserChatFragment";

    private UserChatAdapter userChatAdapter;
    private ArrayList<ChatMessage> chatMessageArray;
    private DatabaseReference roomIdReference;
    private EditText chatText;
    private ListView listView;
    private String opponentNumber;
    private String opponentId;
    private String yourNumber;
    private File mFileTemp;
    private static String TEMP_PHOTO_FILE_NAME;
    private static final int ADD_IMAGE_CAPTURE = 1;
    private static final int ADD_SELECT_PICTURE = 2;
    private Uri mImageCaptureUri = null;
    private StorageReference storageReference;
    private Firebase authReference;
    private Firebase roomReference;
    //private ChatMessage chatMessage;
    private TextView listStickeyHeader;
    private int roomExist = 0;
    private Boolean isChildEventListenerAdd = Boolean.FALSE;
    private String childRoomId;
    private String roomType;
    ArrayList<ChatMessage> chatForwards;

    String mobilenumber;
    @Inject
    FireBaseHelper fireBaseHelper;

    @Inject
    FirebaseService firebaseService;

    @Inject
    MyServiceConnection myServiceConnection;

    @Inject
    YoApi.YoService yoService;

    @Inject
    BaseActivity baseActivity;
    private String opponentImg;

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
        yourNumber = bundle.getString(Constants.YOUR_PHONE_NUMBER);
        opponentImg = bundle.getString(Constants.OPPONENT_CONTACT_IMAGE);

        mobilenumber = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl(BuildConfig.STORAGE_BUCKET);

        if (myServiceConnection.isServiceConnection()) {
            firebaseService.getFirebaseAuth();
        }

        chatForwards = bundle.getParcelableArrayList(Constants.CHAT_FORWARD);
        authReference = fireBaseHelper.authWithCustomToken(preferenceEndPoint.getStringPreference(Constants.FIREBASE_TOKEN));

        if ((childRoomId != null) && (!childRoomId.equals(""))) {
            roomExist = 1;

            roomReference = authReference.child(childRoomId).child(Constants.CHATS);
            registerChildEventListener(roomReference);

            if (chatForwards != null) {
                receiveForward(chatForwards);
            }
        }

        if ((childRoomId == null) && (chatForwards != null)) {
            createRoom("Message", null);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_chat, container, false);

        roomType = getArguments().getString(Constants.TYPE);

        listView = (ListView) view.findViewById(R.id.listView);
        listStickeyHeader = (TextView) view.findViewById(R.id.time_stamp_header);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setOnItemClickListener(this);
        View send = view.findViewById(R.id.send);
        chatText = (EditText) view.findViewById(R.id.chat_text);
        chatMessageArray = new ArrayList<>();
        userChatAdapter = new UserChatAdapter(getActivity(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER), roomType);
        listView.setAdapter(userChatAdapter);

        listView.setOnItemClickListener(this);
        send.setOnClickListener(this);

        return view;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            listView.setStackFromBottom(false);

            listView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    try {
                        if (userChatAdapter != null && userChatAdapter.getCount() > 0) {
                            String headerText = userChatAdapter.getItem(listView.getFirstVisiblePosition()).getStickeyHeader();
                            if (listStickeyHeader != null) {
                                listStickeyHeader.setText("" + headerText);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            });
        } catch (NoClassDefFoundError e) {
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                ChatMessage chatMessage = (ChatMessage) listView.getItemAtPosition(position);
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(Integer.toString(checkedCount));
                userChatAdapter.toggleSelection(position);
                chatMessage.setSelected(true);
                //BUG FIX - 5576	Chat - User shouldn't be able to copy images
                boolean imageSelected = false;
                SparseBooleanArray selected = userChatAdapter.getSelectedIds();
                boolean canDelete = true;
                for (int i = (selected.size() - 1); i >= 0; i--) {
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

                //menu.findItem(R.id.copy).setVisible(false);
                mode.getMenu().findItem(R.id.copy).setVisible(false);
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.delete:

                        SparseBooleanArray selected = userChatAdapter.getSelectedIds();
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {

                                final ChatMessage selectedItem = (ChatMessage) listView.getItemAtPosition(selected.keyAt(i));
                                String timesmp = Long.toString(selectedItem.getTime());
                                roomReference.child(timesmp).removeValue();

                                // Remove  selected items following the ids
                                userChatAdapter.removeItem(selectedItem);
                                chatMessageArray.remove(selectedItem);
                            }
                        }

                        // Close CAB
                        mode.finish();
                        selected.clear();
                        break;
                    case R.id.copy:
                        StringBuilder builder = new StringBuilder();
                        selected = userChatAdapter.getSelectedIds();
                        List<ChatMessage> messagesList = new ArrayList<>();
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

            }
        });
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.call:
                if (opponentNumber != null) {
                    Intent intent = new Intent(getActivity(), OutGoingCallActivity.class);
                    intent.putExtra(OutGoingCallActivity.CALLER_NO, opponentNumber);
                    startActivity(intent);
                }
                break;
            case R.id.attach:
                break;
            case R.id.camera:
                takePicture();
                break;
            case R.id.image:
                getImageFromGallery();
                break;
            case R.id.view_contact:
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                intent.putExtra(Constants.OPPONENT_PHONE_NUMBER, opponentNumber);
                intent.putExtra(Constants.OPPONENT_CONTACT_IMAGE,opponentImg);
                intent.putExtra(Constants.FROM_CHAT_ROOMS,Constants.FROM_CHAT_ROOMS);
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        String message = chatText.getText().toString();
        sendChatMessage(message, Constants.TEXT);
    }

    private void sendChatMessage(String chatMessage, String type) {

        String userId = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        if (chatMessage != null && !TextUtils.isEmpty(chatMessage.trim())) {
            sendChatMessage(chatMessage, userId, type);
            if (chatText != null) {
                if (chatText.getText() != null) {
                    chatText.setText("");
                }
            }

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


        if (type.equals(Constants.TEXT)) {
            chatMessage.setMessage(message);
        } else if (type.equals(Constants.IMAGE)) {
            chatMessage.setImagePath(message);
        }

        if (roomExist == 0) {
            createRoom(message, chatMessage);

        } else {
            chatMessage.setRoomId(childRoomId);
            sendChatMessage(chatMessage);
        }
    }

    private void sendChatMessage(final ChatMessage chatMessage) {
        try {
            String timeStp = Long.toString(chatMessage.getTime());
            Map<String, Object> hashtaghMap = new ObjectMapper().convertValue(chatMessage, Map.class);

            final Firebase roomChildReference = roomReference.child(timeStp);
            roomChildReference.updateChildren(hashtaghMap, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError == null) {
                        // successfully sent
                        chatMessage.setSent(1);
                        Map<String, Object> hashtaghMap = new ObjectMapper().convertValue(chatMessage, Map.class);
                        firebase.updateChildren(hashtaghMap);

                    } else {
                        Log.e(TAG, firebaseError.getMessage());
                    }
                }
            });

        } catch (FirebaseException e) {
            e.printStackTrace();
        }
    }

    private void registerChildEventListener(Firebase roomReference) {
        if (!isChildEventListenerAdd) {
            roomReference.addChildEventListener(this);
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
            TEMP_PHOTO_FILE_NAME = "" + System.currentTimeMillis() + ".jpg";
            if (Environment.MEDIA_MOUNTED.equals(state)) {

                mFileTemp = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        + "/Camera", TEMP_PHOTO_FILE_NAME);
            } else {
                mFileTemp = new File(getActivity().getFilesDir(), TEMP_PHOTO_FILE_NAME);
            }
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mImageCaptureUri = Uri.fromFile(mFileTemp);
            } else {

                //mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
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
        startActivityForResult(intent, ADD_SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case ADD_IMAGE_CAPTURE:
                try {
                    String mPartyPicUri = mFileTemp.getPath();
                    uploadImage(mPartyPicUri);
                } catch (Exception e) {
                }
                break;

            case ADD_SELECT_PICTURE: {
                if (data != null) {
                    Uri targetUri = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    try {
                        Cursor cursor = getActivity().getContentResolver().query(targetUri,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);
                        uploadImage(filePath);
                        if (filePath != null && filePath.length() > 0) {
                            if (filePath.endsWith(".jpg")
                                    || filePath.endsWith(".png")
                                    || filePath.endsWith(".bmp")) {
                                File file = new File(filePath);
                                cursor.close();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            default:
                break;
        }
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * upload image to firebase storage
     *
     * @param path
     */
    private void uploadImage(String path) {

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
                System.out.println("Upload is " + progress + "% done");
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
        String timeStp = Long.toString(timestamp);
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
            chatMessageArray.add(chatMessage);
            userChatAdapter.addItems(chatMessageArray);
            listView.smoothScrollToPosition(userChatAdapter.getCount());

            // Delivered message

            if ((!chatMessage.getSenderID().equalsIgnoreCase(preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER))) && (chatMessage.getDelivered() == 0)) {
                if (getActivity() instanceof ChatActivity) {
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

    }

    @Override
    public void onChildMoved(com.firebase.client.DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        firebaseError.getMessage();
    }

    private void receiveForward(ArrayList<ChatMessage> chatForwards) {

        if (chatForwards != null) {
            for (int i = 0; i < chatForwards.size(); i++) {
                if (chatForwards.get(i).getType().equals(Constants.IMAGE)) {
                    sendChatMessage(chatForwards.get(i).getImagePath(), chatForwards.get(i).getType());
                } else if (chatForwards.get(i).getType().equals(Constants.TEXT)) {
                    sendChatMessage(chatForwards.get(i).getMessage(), chatForwards.get(i).getType());
                }
                chatForwards.get(i).setSelected(false);
            }
        }
    }

    private void createRoom(final String message, final ChatMessage chatMessage) {
        String access = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        List<String> selectedUsers = new ArrayList<>();
        selectedUsers.add(opponentId);
        yoService.getRoomAPI(access, selectedUsers).enqueue(new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                if (response.isSuccessful()) {
                    Room room = response.body();
                    if (room.getFirebaseRoomId() != null) {
                        roomExist = 1;
                        registerChildEventListener(roomReference);

                        if (chatForwards != null) {
                            receiveForward(chatForwards);
                        } else if (chatMessage != null) {

                            chatMessage.setRoomId(room.getFirebaseRoomId());
                            sendChatMessage(chatMessage);
                        }
                        String userId = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
                        update(opponentNumber,room.getFirebaseRoomId());
                        EventBus.getDefault().post(Constants.CHAT_ROOM_REFRESH);
                    }
                } else {
                    if (chatText != null) {
                        //Restore the message if room fails
                        chatText.setText(message);
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
    public void update(String phoneNumber,String roomId) {
        Uri uri = YoAppContactContract.YoAppContactsEntry.CONTENT_URI;
        String where = YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_PHONE_NUMBER + "=?";
        ContentValues contentValues = new ContentValues();
        contentValues.put(YoAppContactContract.YoAppContactsEntry.COLUMN_NAME_FIREBASE_ROOM_ID, roomId);
        getActivity().getContentResolver()
                .update(uri, contentValues, where,
                        new String[]{phoneNumber});
    }
}

