package com.temis.app.service;

import com.temis.app.entity.MessageResponseEntity;

public interface MessagePlatformService {
    void sendMessage(MessageResponseEntity response) throws InterruptedException;
}