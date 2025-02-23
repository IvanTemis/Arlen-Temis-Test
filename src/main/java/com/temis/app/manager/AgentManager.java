package com.temis.app.manager;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FunctionCall;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.temis.app.client.VertexAIClient;
import com.temis.app.client.CloudStorageClient;
import com.temis.app.config.properties.CloudConfigProperties;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.entity.VertexAiContentEntity;
import com.temis.app.placeholder.PlaceholderInjector;
import com.temis.app.repository.VertexAiContentRepository;
import com.temis.app.service.PromptProviderService;
import com.temis.app.utils.VertexAIUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AgentManager {

    private final PromptProviderService promptProviderService;
    private final VertexAIClient vertexAIClient;
    private final PlaceholderInjector placeholderInjector;
    private final VertexAiContentRepository vertexAiContextRepository;


    @Autowired
    public AgentManager(PromptProviderService promptProviderService, VertexAIClient vertexAIClient, PlaceholderInjector placeholderInjector, VertexAiContentRepository vertexAiContextRepository) {
        this.promptProviderService = promptProviderService;
        this.vertexAIClient = vertexAIClient;
        this.placeholderInjector = placeholderInjector;
        this.vertexAiContextRepository = vertexAiContextRepository;
    }

    public String sendSimpleMessageToAgent(String agentId, Content message) throws Exception {
        var prompt = promptProviderService.GetPromptForAgent(agentId);
        return ResponseHandler.getText(vertexAIClient.sendMessage(message, List.of(), prompt)).trim();
    }

    public String sendMessageToAgentAsUser(String agentId, UserEntity user, Content message, Map<String, Object> context) throws Exception {

        var contexts = vertexAiContextRepository.findByUserEntityOrderByCreatedDateAsc(user);

        var history = VertexAIUtils.VertexAiContentEntityToContent(contexts);

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, message, agentId));

        var prompt = promptProviderService.GetPromptForAgent(agentId);

        var response = vertexAIClient.sendMessage(message, history, placeholderInjector.Inject(prompt, new HashMap<>(context){{
            putIfAbsent("user", user);
        }}));

        for (FunctionCall functionCall : ResponseHandler.getFunctionCalls(response)) {
            log.info(functionCall.getName());
        }

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, ResponseHandler.getContent(response), agentId));

        return ResponseHandler.getText(response).trim();
    }
}