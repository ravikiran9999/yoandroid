package com.yo.android.chat.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yo.android.R;
import com.yo.android.helpers.DatabaseHelper;
import com.yo.android.model.ChatMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    DatabaseHelper databaseHelper;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        deleteChatMessage();
        /*for(int i = 0; i < 5; i++ ) {
            insertChatMessage("Welcome" + i);
        }*/
        return view;
    }

    private void insertChatMessage(String message) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        databaseHelper.insertChatObjectToDatabase(chatMessage);
    }

    private void deleteChatMessage() {
        databaseHelper.deleteRowFromDatabase("Welcome");
    }
}
