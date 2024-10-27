package com.temis.app.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class MessageHolderObject {
    String phoneNumber;
    Map<String, String> request;


    public MessageHolderObject(String phoneNumber, Map<String, String> request) {
        this.phoneNumber = phoneNumber;
        this.request = request;
    }
}
