package com.temis.app.service;

import com.temis.app.entity.DocumentEntity;
import com.temis.app.entity.UserEntity;

public interface ClientVirtualAssistantService {

    String respondToUserMessage(String text, DocumentEntity document, UserEntity user, String agentId) throws Exception;
    String generateCompanyIncorporationDraft(String inputJson, UserEntity user) throws Exception;
}