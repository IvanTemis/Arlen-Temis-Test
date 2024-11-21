package com.temis.app.state.with_user;

import com.google.cloud.vertexai.api.Content;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AIChatState extends  StateWithUserTemplate{

    @Autowired
    ChatAIClient chatAIClient;

    @Autowired
    VertexAiContentRepository vertexAiContextRepository;

    @Autowired
    public AIChatState() {
        super(new ArrayList<>(){});
    }

    @Override
    protected boolean ShouldTransitionWithUser(MessageContextEntity message, UserEntity user) {
        return true;
    }

    @Override
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) throws IOException {

        var contexts = vertexAiContextRepository.findByUserEntityOrderByCreatedDateAsc(user);

        List<Content> history = new ArrayList<>();

        for (var item : contexts){
            var c = Content.newBuilder()
                    .setRole(item.getRole().name())
                    .addAllParts(item.getParts().stream().map(p -> {
                        try {
                            return Part.parseFrom(p);
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList())
                    .build();
            history.add(c);
        }

        var content = Content.newBuilder()
                .setRole(VertexAiRole.USER.name())
                .addParts(Part.newBuilder().setText(message.getBody()).build())
                .build();

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, content));

        //TODO: No soporta mensajes sin texto
        var response = chatAIClient.sendMessage(content, history);

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, ResponseHandler.getContent(response)));

        responseBuilder.body(ResponseHandler.getText(response));
    }
}
