package com.temis.app.service.impl;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FileData;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.protobuf.InvalidProtocolBufferException;
import com.temis.app.client.ChatAIClient;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.entity.VertexAiContentEntity;
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
    public String respondToUserMessage(MessageContextEntity message, UserEntity user) throws IOException {
        var partBuilder = Part.newBuilder();

        if(!message.getBody().isEmpty()){
            partBuilder.setText(message.getBody());
        }
        else if (message.getMediaUrl() == null) {
            return "Lo siento, no puedo procesar mensajes vacíos.";
        }
        else{
            partBuilder.setText("documento:");
        }

        var contentBuilder = Content.newBuilder()
                .setRole(VertexAiRole.USER.name())
                .addParts(partBuilder);

        if(message.getMediaUrl() != null && message.getMediaContentType() != null){
            contentBuilder.addParts(Part.newBuilder().setFileData(
                    FileData.newBuilder()
                            .setMimeType(message.getMediaContentType())
                            .setFileUri(message.getMediaUrl())
            ));
        }

        var contexts = vertexAiContextRepository.findByUserEntityOrderByCreatedDateAsc(user);

        var history = VertexAIUtils.VertexAiContentEntityToContent(contexts);

        var content = contentBuilder.build();

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