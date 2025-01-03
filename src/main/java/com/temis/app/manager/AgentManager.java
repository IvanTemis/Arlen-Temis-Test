package com.temis.app.manager;
import com.temis.app.client.ChatAIClient;
import com.temis.app.client.CloudStorageClient;
import com.temis.app.config.properties.CloudConfigProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentManager {
    private final Map<String, ChatAIClient> agents = new ConcurrentHashMap<>();
    private final String projectId;
    private final String location;
    private final String modelName;
    private final CloudStorageClient cloudStorageClient;

    @Autowired
    public AgentManager(CloudConfigProperties cloudConfigProperties, CloudStorageClient cloudStorageClient) {
        this.projectId = cloudConfigProperties.getProjectId();
        this.location = cloudConfigProperties.getLocation();
        this.modelName = cloudConfigProperties.getVertexai().getModelName();
        this.cloudStorageClient = cloudStorageClient;
    }

    public ChatAIClient getAgent(String agentId) throws IOException {
        return agents.computeIfAbsent(agentId, id -> {
            try {
                return new ChatAIClient(projectId, location, modelName, cloudStorageClient, id);
            } catch (IOException e) {
                throw new RuntimeException("Error initializing ChatAIClient for agent: " + id, e);
            }
        });
    }

    public Collection<ChatAIClient> getAllAgents(){
        return agents.values();
    }

    public void updatePrompt(String agentId) throws IOException {
        if (agents.containsKey(agentId)) {
            agents.get(agentId).UpdatePrompt();
        } else {
            throw new IllegalArgumentException("Agent not found: " + agentId);
        }
    }
}