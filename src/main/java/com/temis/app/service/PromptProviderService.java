package com.temis.app.service;

import com.temis.app.entity.UserEntity;

import java.io.IOException;

public interface PromptProviderService {
    String GetPromptForAgent(String agentId) throws IOException;
}