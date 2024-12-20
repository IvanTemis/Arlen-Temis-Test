package com.temis.app.service.impl;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.temis.app.client.ChatAIClient;
import com.temis.app.entity.*;
import com.temis.app.manager.AgentManager;
import com.temis.app.repository.VertexAiContentRepository;
import com.temis.app.utils.VertexAIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.temis.app.service.ClientVirtualAssistantService;

@Service
public class ClientVirtualAssistantServiceImpl implements ClientVirtualAssistantService {
    
    @Autowired
    AgentManager agentManager;

    @Autowired
    VertexAiContentRepository vertexAiContextRepository;


    @Override
    public String respondToUserMessage(String text, DocumentEntity document, UserEntity user, String agentId, String context) throws Exception {
        Content content;
        if(document != null){
            content = VertexAIUtils.ContentWithDocument(text, document.getPath(), document.getFileType());
        }
        else {
            content = VertexAIUtils.ContentWithDocument(text, null, null);
        }

        var contexts = vertexAiContextRepository.findByUserEntityAndAgentIdOrderByCreatedDateAsc(user, agentId);

        var history = VertexAIUtils.VertexAiContentEntityToContent(contexts);

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, content, agentId));

        ChatAIClient chatAIClient = agentManager.getAgent(agentId);
        var response = chatAIClient.sendMessage(content, history, context);

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, ResponseHandler.getContent(response), agentId));

        return ResponseHandler.getText(response);
    }
}