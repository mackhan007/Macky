package com.example.macky_chat_app;

public class messages extends current_push_id {
    private String message,type,from,time,date,push_id;
    private long timestamp;
    public messages(){
    }

    public messages(String message, String type, String time, long timestamp, String from, String date, String push_id) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.timestamp = timestamp;
        this.from = from;
        this.date = date;
        this.push_id = push_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getPush_id() {
        return push_id;
    }

    public void setPush_id(String push_id) {
        this.push_id = push_id;
    }
}
