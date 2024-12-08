package com.temis.app.state.with_user;

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
import com.temis.app.repository.UserRepository;
import com.temis.app.repository.VertexAiContentRepository;
import com.temis.app.service.ClientVirtualAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ClientVirtualAssistantState extends  StateWithUserTemplate{

    @Autowired
    private ClientVirtualAssistantService clientVirtualAssistantService;

    @Autowired
    public ClientVirtualAssistantState() {
        super(new ArrayList<>(){});
    }

    @Override
    protected boolean ShouldTransitionWithUser(MessageContextEntity message, UserEntity user) {
        return true;
    }

    @Override
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) throws IOException {
        if (message.getMediaUrl() == null && message.getBody().isEmpty()) {
            responseBuilder.body("Lo siento, no puedo procesar mensajes vacíos.");
            return;
        }

        String text = message.getBody();
        if(text == null || text.isEmpty()){
            text = "documento:";
        }

        responseBuilder.body(clientVirtualAssistantService.respondToUserMessage(text, message.getDocumentEntity(), user));
    }
}
