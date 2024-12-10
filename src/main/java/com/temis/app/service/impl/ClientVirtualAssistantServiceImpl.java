package com.temis.app.service.impl;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FileData;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.protobuf.InvalidProtocolBufferException;
import com.temis.app.client.ChatAIClient;
import com.temis.app.entity.*;
import com.temis.app.model.VertexAiRole;
import com.temis.app.repository.VertexAiContentRepository;
import com.temis.app.utils.VertexAIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.temis.app.service.ClientVirtualAssistantService;

import java.io.IOException;

@Service
public class ClientVirtualAssistantServiceImpl implements ClientVirtualAssistantService {
    @Autowired
    ChatAIClient chatAIClient;

    @Autowired
    VertexAiContentRepository vertexAiContextRepository;


    @Override
    public String respondToUserMessage(String text, DocumentEntity document, UserEntity user) throws Exception {
        Content content;
        if(document != null){
            content = VertexAIUtils.ContentWithDocument(text, document.getPath(), document.getFileType());
        }
        else {
            content = VertexAIUtils.ContentWithDocument(text, null, null);
        }

        var contexts = vertexAiContextRepository.findByUserEntityOrderByCreatedDateAsc(user);

        var history = VertexAIUtils.VertexAiContentEntityToContent(contexts);

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, content));

        var response = chatAIClient.sendMessage(content, history,
                "\n" +
                        "Contexto de la conversación:\n" +
                        "\t- Nombre del usuario: " + user.getSuitableName() + ".\n" +
                        "\t- Fecha y Hora actual: " + java.time.LocalDateTime.now() + ".\n" +
                        "\t- Fecha y Hora de la última interacción: " + (user.getLastInteractionDate() == null ? "Nunca" : user.getLastInteractionDate()) + ".\n"
        );

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, ResponseHandler.getContent(response)));

        return ResponseHandler.getText(response);
    }
}