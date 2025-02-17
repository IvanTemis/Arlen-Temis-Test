package com.temis.app.service.impl;

import com.temis.app.client.CloudStorageClient;
import com.temis.app.service.PromptProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class CloudStoragePromptProviderServiceImpl implements PromptProviderService {
    @Autowired
    private CloudStorageClient cloudStorageClient;

    @Override
    public String GetPromptForAgent(String agentId) throws IOException {
        log.info("Updating prompt for agent: {}", agentId);
        String promptPath = String.format("gs://temis-prd-storage/prompts/%s.txt", agentId);
        return cloudStorageClient.ReadFile(promptPath);
    }
}
