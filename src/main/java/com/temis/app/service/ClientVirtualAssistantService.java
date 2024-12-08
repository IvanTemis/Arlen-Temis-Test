package com.temis.app.service;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.entity.UserEntity;

import java.io.IOException;

public interface ClientVirtualAssistantService {

    String respondToUserMessage(MessageContextEntity message, UserEntity user) throws IOException;
}