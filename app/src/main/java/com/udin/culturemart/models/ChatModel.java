package com.udin.culturemart.models;

public class ChatModel {
    String id;
    UserModel with;
    MessageModel lastChat;

    public ChatModel(String id, UserModel with, MessageModel lastChat) {
        this.id = id;
        this.with = with;
        this.lastChat = lastChat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserModel getWith() {
        return with;
    }

    public void setWith(UserModel with) {
        this.with = with;
    }

    public MessageModel getLastChat() {
        return lastChat;
    }

    public void setLastChat(MessageModel lastChat) {
        this.lastChat = lastChat;
    }
}
