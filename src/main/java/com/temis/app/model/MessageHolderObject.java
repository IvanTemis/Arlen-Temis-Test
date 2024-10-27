package com.temis.app.model;

import com.temis.app.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Map;

@Getter
public class MessageHolderObject {
    @Setter @Nullable
    UserEntity userEntity;

    String phoneNumber;
    String nickName;
    Map<String, String> request;


    public MessageHolderObject(String phoneNumber, String nickName, Map<String, String> request) {
        this.phoneNumber = phoneNumber;
        this.nickName = nickName;
        this.request = request;
    }
}
