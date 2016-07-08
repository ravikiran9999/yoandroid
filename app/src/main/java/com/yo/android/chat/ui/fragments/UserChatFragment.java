package com.yo.android.chat.ui.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.yo.android.R;
import com.yo.android.adapters.UserChatAdapter;
import com.yo.android.chat.firebase.Clipboard;
import com.yo.android.model.ChatMessage;
import com.yo.android.ui.DialerActivity;
import com.yo.android.util.Constants;
import com.yo.android.voip.OutGoingCallActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnItemLongClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserChatFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemLongClickListener, View.OnLongClickListener {

    private UserChatAdapter userChatAdapter;
    private ArrayList<ChatMessage> chatMessageArray;
    private DatabaseReference roomIdReference;
    private EditText chatText;
    private ListView listView;
    private String opponentNumber;

    public UserChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        String child = bundle.getString(Constants.CHAT_ROOM_ID);
        opponentNumber = bundle.getString(Constants.OPPONENT_PHONE_NUMBER);
        DatabaseReference roomReference = FirebaseDatabase.getInstance().getReference(Constants.ROOM_ID);
        if (child != null) {
            roomIdReference = roomReference.child(child);
        }
        getMessageFromDatabase();
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
        listView.setOnItemLongClickListener(this);
        send.setOnClickListener(this);
        chatText.setOnLongClickListener(this);
        return view;
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
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        String message = chatText.getText().toString();
        String userId = preferenceEndPoint.getStringPreference(Constants.PHONE_NUMBER);
        if (!TextUtils.isEmpty(message)) {
            sendChatMessage(message, userId);
            if (chatText.getText() != null) {
                chatText.setText("");
            }
        }
    }

    private void sendChatMessage(@NonNull String message, @NonNull String userId) {
        long timestamp = System.currentTimeMillis();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setTime(timestamp);
        chatMessage.setSenderID(userId);
        //chatMessage.setTimeStamp(ServerValue.TIMESTAMP);

        DatabaseReference reference = roomIdReference.push();
        reference.setValue(chatMessage);
        // getTimeFromFireBaseServer(reference);
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
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ChatMessage chatMessage = (ChatMessage) listView.getItemAtPosition(position);
        new Clipboard(getActivity()).copy(chatMessage.getMessage());
        return true;
    }

    @Override
    public boolean onLongClick(View v) {

        chatText.setText(new Clipboard(getActivity()).paste(v));
        return true;
    }
}
