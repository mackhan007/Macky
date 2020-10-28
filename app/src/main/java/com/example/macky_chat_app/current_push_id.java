package com.example.macky_chat_app;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

public class current_push_id {
    @Exclude
    public String current_push_id,current_user_id,from_user_id;

    public <T extends current_push_id> T withId(@NonNull final String message_id,@NonNull final String current_user_id,@NonNull final String from_user_id){
        this.current_push_id = message_id;
        this.current_user_id = current_user_id;
        this.from_user_id = from_user_id;
        return (T) this;
    }
}
