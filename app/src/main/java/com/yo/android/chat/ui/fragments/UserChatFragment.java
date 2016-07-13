package com.yo.android.chat.ui.fragments;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yo.android.R;
import com.yo.android.adapters.UserChatAdapter;
import com.yo.android.chat.firebase.Clipboard;
import com.yo.android.model.ChatMessage;
import com.yo.android.model.ChatRoom;
import com.yo.android.util.Constants;
import com.yo.android.voip.OutGoingCallActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserChatFragment extends BaseFragment implements View.OnClickListener, View.OnLongClickListener, DatabaseReference.CompletionListener, AdapterView.OnItemClickListener {

    private static final String TAG = "UserChatFragment";

    private UserChatAdapter userChatAdapter;
    private ArrayList<ChatMessage> chatMessageArray;
    private DatabaseReference roomIdReference;
    private EditText chatText;
    private ListView listView;
    private String opponentNumber;
    private String yourNumber;
    private File mFileTemp;
    private static String TEMP_PHOTO_FILE_NAME;
    private static final int ADD_IMAGE_CAPTURE = 1;
    private static final int ADD_SELECT_PICTURE = 2;
    private boolean isContextualMenuEnable = false;
    private Uri mImageCaptureUri = null;
    private StorageReference storageReference;

    public UserChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        String child = bundle.getString(Constants.CHAT_ROOM_ID);
        opponentNumber = bundle.getString(Constants.OPPONENT_PHONE_NUMBER);
        yourNumber = bundle.getString(Constants.YOUR_PHONE_NUMBER);

        DatabaseReference roomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM_ID);
        if (child != null) {
            roomIdReference = roomReference.child(child);
            roomIdReference.keepSynced(true);
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://samplefcm-ce2c6.appspot.com");

        getMessageFromDatabase();
        ChatMessage chatForward = bundle.getParcelable(Constants.CHAT_FORWARD);

        if (chatForward != null) {
            if (chatForward.getType().equals(Constants.IMAGE)) {
                sendChatMessage(chatForward.getImagePath(), chatForward.getType());
            } else if (chatForward.getType().equals(Constants.TEXT)) {
                sendChatMessage(chatForward.getMessage(), chatForward.getType());
            }
            chatForward.setSelected(false);
        }

    setHasOptionsMenu(true);
}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_chat, container, false);

        listView = (ListView) view.findViewById(R.id.listView);
        View send = view.findViewById(R.id.send);
        chatText = (EditText) view.findViewById(R.id.chat_text);
        chatMessageArray = new ArrayList<>();
        userChatAdapter = new UserChatAdapter(getActivity().getApplicationContext(), preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER));
        listView.setAdapter(userChatAdapter);
        listView.setOnItemClickListener(this);
        send.setOnClickListener(this);
        chatText.setOnLongClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        //listView.setStackFromBottom(true);
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
        if (!TextUtils.isEmpty(chatMessage)) {

            sendChatMessage(chatMessage, userId, type);

            if (chatText != null) {
                if (chatText.getText() != null) {
                    chatText.setText("");
                }
            }

        }
    }

    private void sendChatMessage(@NonNull String message, @NonNull String userId, @NonNull String type) {

        long timestamp = System.currentTimeMillis();
        String timeStp = Long.toString(timestamp);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(type);
        chatMessage.setTime(timestamp);
        chatMessage.setSenderID(userId);

        if (type.equals(Constants.TEXT)) {
            chatMessage.setMessage(message);
        } else if (type.equals(Constants.IMAGE)) {
            chatMessage.setImagePath(message);
        }

        roomIdReference.child(timeStp).setValue(chatMessage, this);
    }

    private void getTimeFromFireBaseServer(DatabaseReference reference) {
        //Get the server time stamp
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long timestamp = (Long) (snapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        reference.setValue(ServerValue.TIMESTAMP);

    }

    private void getMessageFromDatabase() {

        // Retrieve new posts as they are added to the database
        roomIdReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    chatMessageArray.add(chatMessage);
                    userChatAdapter.addItems(chatMessageArray);
                    listView.smoothScrollToPosition(userChatAdapter.getCount());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final ChatMessage chatMessage = (ChatMessage) listView.getItemAtPosition(position);
        isContextualMenuEnable = true;
        getActivity().invalidateOptionsMenu();

        // parent.getChildAt(position).setBackgroundColor(Color.BLUE);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                //listView.getChildAt(position).setBackgroundColor(Color.BLUE);
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(Integer.toString(checkedCount));
                // Calls  toggleSelection method from ListViewAdapter Class
                userChatAdapter.toggleSelection(position);

                chatMessage.setSelected(true);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_change, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.delete:

                        SparseBooleanArray selected = userChatAdapter.getSelectedIds();
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {

                                //parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                                //view.setBackgroundColor(Color.GREEN);

                                ChatMessage selectedItem = userChatAdapter.getItem(selected.keyAt(i));
                                String timesmp = Long.toString(selectedItem.getTime());
                                roomIdReference.child(timesmp).removeValue();

                                // Remove  selected items following the ids
                                userChatAdapter.removeItem(selectedItem);
                            }
                        }

                        // Close CAB
                        mode.finish();
                        selected.clear();
                        break;
                    case R.id.copy:
                        new Clipboard(getActivity()).copy(chatMessage.getMessage());
                        break;
                    case R.id.forward:
                        if (chatMessage.isSelected()) {
                            forwardMessage(chatMessage);
                        }
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
    }

    @Override
    public boolean onLongClick(View v) {
        chatText.setText(new Clipboard(getActivity()).paste(v));
        return true;
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

        }
    }

    // open gallery
    private void getImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            intent.setType("image/*,video/*");
        } else {
            intent.setType("image/*");
        }

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

        StorageReference riversRef = storageReference.child("images/" + file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file, metadata);

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
                sendImage(downloadUrl.getLastPathSegment());
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

        roomIdReference.child(timeStp).setValue(chatMessage, this);

    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        if (databaseError == null) {
            // successfully inserted to database
        } else {
            Log.e(TAG, databaseError.getMessage());
        }
    }

    private void forwardMessage(ChatMessage message) {
        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.CHAT_FORWARD, message);
        chatFragment.setArguments(args);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, chatFragment)
                .commit();
        getActivity().finish();
    }
}

