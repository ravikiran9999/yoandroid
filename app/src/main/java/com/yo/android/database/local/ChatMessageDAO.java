package com.yo.android.database.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

import com.yo.android.database.model.ChatMessage;

@Dao
public interface ChatMessageDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insetChatMessage(ChatMessage... chatMessages);

    @Update
    void updateChatMessage(ChatMessage... chatMessages);

}
