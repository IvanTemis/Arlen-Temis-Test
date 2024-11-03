package com.temis.app.model;

import com.temis.app.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Map;

@Builder
@Getter
public class MessageContext {
    @Setter @Nullable
    UserEntity userEntity;

    String phoneNumber;
    String nickName;
    Map<String, String> request;


    public MessageContext(Map<String, String> request) {
        this.phoneNumber = phoneNumber;
        this.nickName = nickName;
        this.request = request;
    }
}
