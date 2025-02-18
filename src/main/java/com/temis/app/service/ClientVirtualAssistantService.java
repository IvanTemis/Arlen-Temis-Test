package com.temis.app.service;

import com.google.cloud.vertexai.api.Content;
import com.temis.app.entity.DocumentEntity;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.UserEntity;

public interface ClientVirtualAssistantService {

    String respondToUserMessage(Content content, UserEntity user, String agentId, MessageContextEntity messageContext) throws Exception;
    String generateCompanyIncorporationDraft(String inputJson, UserEntity user) throws Exception;

}