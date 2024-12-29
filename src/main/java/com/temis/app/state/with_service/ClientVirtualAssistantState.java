package com.temis.app.state.with_service;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FileData;
import com.google.cloud.vertexai.api.Part;
import com.temis.app.entity.*;
import com.temis.app.exception.JSONNotFoundException;
import com.temis.app.model.ServiceStage;
import com.temis.app.model.VertexAiRole;
import com.temis.app.repository.StageContextRepository;
import com.temis.app.service.ClientVirtualAssistantService;
import com.temis.app.utils.VertexAIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.regex.Pattern;

@Component
public class ClientVirtualAssistantState extends StateWithServiceTemplate {

    static final String END_STAGE_1 = "END_STAGE_1";


    Pattern jsonPattern = Pattern.compile("(\\[*\\{(?:.*|[\\n\\t\\r]*)+?\\}\\]*)",
            Pattern.CASE_INSENSITIVE);

    @Autowired
    private ClientVirtualAssistantService clientVirtualAssistantService;

    @Autowired
    private StageContextRepository stageContextRepository;

    @Autowired
    public ClientVirtualAssistantState() {
        super(new ArrayList<>(){});
    }

    @Override
    protected boolean ShouldTransitionWithService(MessageContextEntity message, UserEntity user, ServiceEntity service) {
        return true;
    }

    @Override
    protected void ExecuteWithService(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user, ServiceEntity service) throws Exception {


        int processed = 0;
        var aiContentBuilder = Content.newBuilder().setRole(VertexAiRole.USER.name());
        for (MessageContentEntity content : message.getMessageContents()) {
            if(content.getDocumentEntity() == null && content.getBody().isEmpty()) continue;

            String text = content.getBody();
            if(text == null || text.isEmpty()){
                text = "documento";

                if(content.getDocumentEntity().getDocumentType() != null){
                    text += " ";
                    text += content.getDocumentEntity().getDocumentType().getName();
                }

                text += ":";
            }

            aiContentBuilder.addParts(Part.newBuilder().setText(text));

            if(content.getDocumentEntity() != null) {
                aiContentBuilder.addParts(Part.newBuilder().setFileData(
                        FileData.newBuilder()
                                .setMimeType(content.getDocumentEntity().getFileType())
                                .setFileUri(content.getDocumentEntity().getPath())
                ));
            }

            processed++;
        }

        if (processed == 0) {
            responseBuilder.body("Lo siento, no puedo procesar mensajes vacíos.");
            return;
        }

        switch (service.getServiceStage()){
            case SOCIETY_IDENTIFICATION -> {
                String result = clientVirtualAssistantService.respondToUserMessage(aiContentBuilder.build(), user,ServiceStage.SOCIETY_IDENTIFICATION.getAgentId(),
                        "\nContexto de la conversación:\n" +
                                "\t- Nombre del usuario: " + user.getSuitableName() + ".\n" +
                                "\t- Fecha y Hora actual: " + java.time.LocalDateTime.now() + ".\n" +
                                "\t- Fecha y Hora de la última interacción: " +
                                (user.getLastInteractionDate() == null ? "Nunca" : user.getLastInteractionDate()) + ".\n");

                if(result.contains(END_STAGE_1)){
                    result = ExtractEndMessage(result, ServiceStage.SOCIETY_IDENTIFICATION, ServiceStage.DOCUMENT_COLLECTION, service);
                }

                responseBuilder.body(result);
            }
            case DOCUMENT_COLLECTION -> {
                var stageContexts = stageContextRepository.findByServiceAndTargetStage(service, ServiceStage.DOCUMENT_COLLECTION).stream().map(StageContextEntity::getContext).toList();

                String result = clientVirtualAssistantService.respondToUserMessage(aiContentBuilder.build(), user,ServiceStage.DOCUMENT_COLLECTION.getAgentId(),
                        "\nContexto de la conversación:\n" +
                                "\t- Nombre del usuario: " + user.getSuitableName() + ".\n" +
                                "\t- Fecha y Hora actual: " + java.time.LocalDateTime.now() + ".\n" +
                                "\t- Fecha y Hora de la última interacción: " +
                                (user.getLastInteractionDate() == null ? "Nunca" : user.getLastInteractionDate()) + ".\n" +
                                String.join("\\n", stageContexts));

                if(result.contains(END_STAGE_1)){
                    result = ExtractEndMessage(result, ServiceStage.DOCUMENT_COLLECTION, ServiceStage.COMPANY_INCORPORATION, service);
                }

                responseBuilder.body(result);
            }
            default -> {
                throw new Exception("Estado " + service.getServiceStage().name() + " es inválido.");
            }
        }


    }

    private String ExtractEndMessage(String message, ServiceStage current, ServiceStage next, ServiceEntity service) throws JSONNotFoundException {
        var result = message.replace(END_STAGE_1, "");

        service.setServiceStage(next);

        var matcher = jsonPattern.matcher(result);

        if(matcher.find()){

            var json = matcher.group(1);

            log.info("Se finalizó {} con json: {}", current, json);

            var stageContext = new StageContextEntity(null, current, next, json, null, true, service);

            stageContextRepository.save(stageContext);

            result = result.replace(json, "").replace("```json", "").replace("```", "").trim();
        }
        else{
            throw new JSONNotFoundException("Se intentó finalizar la etapa SOCIETY_IDENTIFICATION del agente pero no se encontró un JSON válido");
        }

        return result;
    }
}
