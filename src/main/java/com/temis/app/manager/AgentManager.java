package com.temis.app.manager;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FunctionCall;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.temis.app.client.VertexAIClient;
import com.temis.app.client.CloudStorageClient;
import com.temis.app.config.properties.CloudConfigProperties;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.placeholder.PlaceholderInjector;
import com.temis.app.service.PromptProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AgentManager {

    private final PromptProviderService promptProviderService;
    private final VertexAIClient vertexAIClient;
    private final PlaceholderInjector placeholderInjector;

    @Autowired
    public AgentManager(PromptProviderService promptProviderService, VertexAIClient vertexAIClient, PlaceholderInjector placeholderInjector) {
        this.promptProviderService = promptProviderService;
        this.vertexAIClient = vertexAIClient;
        this.placeholderInjector = placeholderInjector;
    }

    public GenerateContentResponse sendSimpleMessageToAgent(String agentId, Content message) throws Exception {
        var prompt = promptProviderService.GetPromptForAgent(agentId);
        return vertexAIClient.sendMessage(message, List.of(), prompt);
    }

    public GenerateContentResponse sendMessageToAgent(String agentId, Content message, @Nullable List<Content> history, MessageContextEntity messageContext) throws Exception {
        var prompt = promptProviderService.GetPromptForAgent(agentId);

        var response = vertexAIClient.sendMessage(message, history, placeholderInjector.Inject(prompt, messageContext));

        for (FunctionCall functionCall : ResponseHandler.getFunctionCalls(response)) {
            log.info(functionCall.getName());
        }

        //TODO: Mover todo el handling de la response aquí y retornar solamente la respuesta.

        return response;
    }
}