package com.yo.android.chat.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yo.android.R;
import com.yo.android.adapters.UserChatAdapter;
import com.yo.android.helpers.DatabaseHelper;
import com.yo.android.model.ChatMessage;
import com.yo.android.util.DatabaseConstant;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserChatFragment extends BaseFragment implements View.OnClickListener {

    private DatabaseHelper databaseHelper;
    private UserChatAdapter userChatAdapter;
    private ArrayList<ChatMessage> chatMessageArray;
    DatabaseReference roomIdReference;
    TextView chatText;

    public UserChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getActivity().getApplicationContext());

        Bundle bundle = this.getArguments();
        String child = bundle.getString(DatabaseConstant.CHAT_ROOM_ID);
        DatabaseReference roomReference = FirebaseDatabase.getInstance().getReference(DatabaseConstant.ROOM_ID);
        if (child != null) {
            roomIdReference = roomReference.child(child);
        }
        getMessageFromDatabase();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_chat, container, false);

        ListView listView = (ListView) view.findViewById(R.id.listView);
        Button button = (Button) view.findViewById(R.id.send);
        chatText = (TextView) view.findViewById(R.id.chat_text);
        chatMessageArray = new ArrayList<>();
        userChatAdapter = new UserChatAdapter(getActivity().getApplicationContext(), preferenceEndPoint.getStringPreference("phone"));
        listView.setAdapter(userChatAdapter);

        button.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        String message = chatText.getText().toString();
        String userId = preferenceEndPoint.getStringPreference("phone");
        sendChatMessage(message, userId);
        if (chatText.getText() != null) {
            chatText.setText("");
        }
    }

    private void sendChatMessage(@NonNull String message, @NonNull String userId) {
        long timestamp = System.currentTimeMillis();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setTime(timestamp);
        chatMessage.setSenderID(userId);

        DatabaseReference reference = roomIdReference.push();
        reference.setValue(chatMessage);
    }

    private void getMessageFromDatabase() {

        // Retrieve new posts as they are added to the database
        roomIdReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                chatMessageArray.add(chatMessage);
                userChatAdapter.addItems(chatMessageArray);
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
}
