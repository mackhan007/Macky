package com.example.macky_chat_app;

public class friendRequest {
    String request_type;
    public friendRequest(){
    }

    public friendRequest(String request_type) {
        this.request_type = request_type;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
