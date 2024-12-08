package com.temis.app.service;

import com.temis.app.entity.DocumentEntity;
import com.temis.app.entity.UserEntity;

import java.io.IOException;

public interface ClientVirtualAssistantService {

    String respondToUserMessage(String text, DocumentEntity document, UserEntity user) throws IOException;
}