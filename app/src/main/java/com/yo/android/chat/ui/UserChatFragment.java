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
public class UserChatFragment extends Fragment implements View.OnClickListener {

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
        DatabaseReference roomReference = FirebaseDatabase.getInstance().getReference(DatabaseConstant.ROOM_ID);
        roomIdReference = roomReference.child(bundle.getString(DatabaseConstant.CHAT_ROOM_ID));
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
        userChatAdapter = new UserChatAdapter(getActivity().getApplicationContext());
        listView.setAdapter(userChatAdapter);

        button.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        String message = chatText.getText().toString();

        //sendChatMessage(message, userName);
        sendChatMessage(message);
        if (chatText.getText() != null) {
            chatText.setText("");
        }
    }

    //private void sendChatMessage(@NonNull String message, @NonNull String userName) {
    private void sendChatMessage(@NonNull String message) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        //chatMessage.setSenderID(userName);

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
