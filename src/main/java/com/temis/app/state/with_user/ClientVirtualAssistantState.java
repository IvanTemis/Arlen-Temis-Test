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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ClientVirtualAssistantState extends  StateWithUserTemplate{

    static final String END_STAGE = "END_STAGE_1";

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
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) throws Exception {
        if (message.getMediaUrl() == null && message.getBody().isEmpty()) {
            responseBuilder.body("Lo siento, no puedo procesar mensajes vacíos.");
            return;
        }

        String text = message.getBody();
        if(text == null || text.isEmpty()){
            text = "documento";

            if(message.getDocumentEntity() != null && message.getDocumentEntity().getDocumentType() != null){
                text += " ";
                text += message.getDocumentEntity().getDocumentType().getName();
            }

            text += ":";
        }

        String result = clientVirtualAssistantService.respondToUserMessage(text, message.getDocumentEntity(), user,"agent123");

        if(result.contains(END_STAGE)){
            var endIndex = result.indexOf(END_STAGE);

            String json = result.substring(endIndex + END_STAGE.length()).trim();


            log.info("Se finalizó primer etapa con json: {}", json);

            result = result.substring(0, endIndex);
        }

        responseBuilder.body(result);
    }
}
