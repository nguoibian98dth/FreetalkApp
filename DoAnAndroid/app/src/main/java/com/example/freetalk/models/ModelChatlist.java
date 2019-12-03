package com.example.freetalk.models;

public class ModelChatlist {
    String id; //id nay giup ta get chatlist, sender/receiver uid

    public ModelChatlist(String id) {
        this.id = id;
    }

    public ModelChatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
