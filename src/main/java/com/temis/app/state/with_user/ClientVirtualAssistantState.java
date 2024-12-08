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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
        var partBuilder = Part.newBuilder();

        if(!message.getBody().isEmpty()){
            partBuilder.setText(message.getBody());
        }
        else if (message.getMediaUrl() == null) {
            responseBuilder.body("Lo siento, no puedo procesar mensajes que no sean texto.");
            return;
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

        List<Content> history = new ArrayList<>();

        for (var item : contexts) {
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

        var content = contentBuilder.build();

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, content));

        String name = user.getNickName();

        if (user.getFirstName() != null) {
            name = user.getFirstName();

            if (user.getLastName() != null) {
                name += " " + user.getLastName();
            }
        }

        var response = chatAIClient.sendMessage(content, history,
                "\n" +
                        "Contexto de la conversación:\n" +
                        "\t- Nombre del usuario: " + name + ".\n" +
                        "\t- Fecha y Hora actual: " + java.time.LocalDateTime.now() + ".\n" +
                        "\t- Fecha y Hora de la última interacción: " + (user.getLastInteractionDate() == null ? "Nunca" : user.getLastInteractionDate()) + ".\n"
        );

        String rawResponse = ResponseHandler.getText(response);
        //String conciseResponse = chatAIClient.filterResponse(rawResponse);

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, ResponseHandler.getContent(response)));

        responseBuilder.body(rawResponse);
    }
}
