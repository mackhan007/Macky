package com.example.macky_chat_app;

public class chats {
    long timestamp;
    public chats(){
    }

    public chats(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
