package com.udin.culturemart.models;

import com.google.firebase.Timestamp;

public class MessageModel {
    Timestamp datetime;
    String from;
    String message;

    public MessageModel(Timestamp datetime, String from, String message) {
        this.datetime = datetime;
        this.from = from;
        this.message = message;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public void setDatetime(Timestamp datetime) {
        this.datetime = datetime;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
