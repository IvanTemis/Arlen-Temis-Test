package com.temis.app.service;

import com.temis.app.entity.MessageResponseEntity;

public interface MessageService {

    void sendResponseToUser(MessageResponseEntity response);
    
}