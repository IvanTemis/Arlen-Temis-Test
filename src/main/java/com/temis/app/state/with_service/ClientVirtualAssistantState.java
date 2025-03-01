package com.temis.app.state.with_service;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FileData;
import com.google.cloud.vertexai.api.Part;
import com.temis.app.client.GoogleCalendarClient;
import com.temis.app.entity.*;
import com.temis.app.exception.JSONNotFoundException;
import com.temis.app.model.MessageParts;
import com.temis.app.model.ServiceStage;
import com.temis.app.model.VertexAiRole;
import com.google.api.services.calendar.model.Event;
import com.temis.app.repository.StageContextRepository;
import com.temis.app.service.ClientVirtualAssistantService;
import com.temis.app.service.SchedulerService;
import com.temis.app.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Component
public class ClientVirtualAssistantState extends StateWithServiceTemplate {

    static final String END_STAGE_1 = "END_STAGE_1";

    Pattern jsonPattern = Pattern.compile("(\\[*\\{(?:.*|[\\n\\t\\r]*)+?\\}\\]*)",
            Pattern.CASE_INSENSITIVE);


    @Autowired
    private SchedulerService schedulerService;
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

        if (message.isEmpty()) {
            responseBuilder.addContent("Lo siento, no puedo procesar mensajes vacíos.");
            return;
        }

        Content content = message.toVertexAiContent();

        String result = "You shouldn't be here.";
        var serviceStage = service.getServiceStage();

        switch (serviceStage) {
            case SOCIETY_IDENTIFICATION -> {
                result = clientVirtualAssistantService.respondToUserMessage(message, user, serviceStage.getAgentId());

                if (result.contains(END_STAGE_1)) {
                    MessageParts messageParts = ExtractEndMessage(result, serviceStage, ServiceStage.DOCUMENT_COLLECTION, service);
                    result = messageParts.getText();
                }
            }
            case DOCUMENT_COLLECTION -> {
                result = clientVirtualAssistantService.respondToUserMessage(message, user, serviceStage.getAgentId());

                if (result.contains(END_STAGE_1)) {
                    MessageParts messageParts = ExtractEndMessage(result, serviceStage, ServiceStage.ORGANIZATIONAL_STRUCTURE, service);
                    result = messageParts.getText();
                }
            }
            case ORGANIZATIONAL_STRUCTURE -> {
                result = clientVirtualAssistantService.respondToUserMessage(message, user, serviceStage.getAgentId());

                if (result.contains(END_STAGE_1)) {
                    MessageParts messageParts = ExtractEndMessage(result, serviceStage, ServiceStage.PAYMENT_COLLECTION, service);
                    result = messageParts.getText();
                }
            }
            case PAYMENT_COLLECTION -> {
                result = clientVirtualAssistantService.respondToUserMessage(message, user, serviceStage.getAgentId());

                if (result.contains(END_STAGE_1)) {
                    MessageParts messageParts = ExtractEndMessage(result, serviceStage, ServiceStage.COMPANY_INCORPORATION, service);
                    result = messageParts.getText();

                    schedulerService.ScheduleDraftGeneration(messageParts.getJson(), user);
                }
            }
            case COMPANY_INCORPORATION -> {
                result = "Muchas gracias.?LL? La Lic. Zélica Castro se pondrá en contacto contigo en breve.";
            }
            default -> throw new Exception("Estado " + service.getServiceStage().name() + " es inválido.");
        }

        //Este código se encarga de construir las respuestas al usuario. Tiene que ser ejecutado por cada ejecución de este método o no va a responder nada.
        var sentences = TextUtils.splitIntoSentences(result);
        for (String sentence : sentences) {
            responseBuilder.addContent(sentence);
        }
        if(sentences.isEmpty()){
            responseBuilder.addContent("[(El agente respondió vacío)]");
        }
    }

    private MessageParts ExtractEndMessage(String message, ServiceStage current, ServiceStage next, ServiceEntity service) throws JSONNotFoundException {
        String result = message.replace(END_STAGE_1, "").trim();

        service.setServiceStage(next);

        var matcher = jsonPattern.matcher(result);
        String extractedJson = null;

        if (matcher.find()) {
            extractedJson = matcher.group(1).trim();

            log.info("Se finalizó {} con JSON: {}", current, extractedJson);

            var stageContext = new StageContextEntity(
                    null, current, next, extractedJson, null, true, service);
            stageContextRepository.save(stageContext);

            result = result.replace(extractedJson, "").replace("```json", "").replace("```", "").trim();
        } else {
            /*throw new JSONNotFoundException("Se intentó finalizar la etapa " + current.name() +
                    " del agente pero no se encontró un JSON válido");*/
        }

        return new MessageParts(result, extractedJson);
    }
}