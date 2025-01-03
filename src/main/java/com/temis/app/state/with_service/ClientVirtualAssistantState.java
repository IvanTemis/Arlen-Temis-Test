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
        for (MessageContextContentEntity content : message.getMessageContents()) {
            if (content.getDocumentEntity() == null && content.getBody().isEmpty()) continue;

            String text = content.getBody();
            if (text == null || text.isEmpty()) {
                text = "documento";

                if (content.getDocumentEntity().getDocumentType() != null) {
                    text += " ";
                    text += content.getDocumentEntity().getDocumentType().getName();
                }

                text += ":";
            }

            aiContentBuilder.addParts(Part.newBuilder().setText(text));

            if (content.getDocumentEntity() != null) {
                aiContentBuilder.addParts(Part.newBuilder().setFileData(
                        FileData.newBuilder()
                                .setMimeType(content.getDocumentEntity().getFileType())
                                .setFileUri(content.getDocumentEntity().getPath())
                ));
            }

            processed++;
        }

        if (processed == 0) {
            responseBuilder.addContent("Lo siento, no puedo procesar mensajes vacíos.");
            return;
        }

        String result = "You shouldn't be here.";

        switch (service.getServiceStage()) {
            case SOCIETY_IDENTIFICATION -> {
                result = clientVirtualAssistantService.respondToUserMessage(aiContentBuilder.build(), user, ServiceStage.SOCIETY_IDENTIFICATION.getAgentId(),
                        "\nContexto de la conversación:\n" +
                                "\t- Nombre del usuario: " + user.getSuitableName() + ".\n" +
                                "\t- Fecha y Hora actual: " + java.time.LocalDateTime.now() + ".\n" +
                                "\t- Fecha y Hora de la última interacción: " +
                                (user.getLastInteractionDate() == null ? "Nunca" : user.getLastInteractionDate()) + ".\n");

                if (result.contains(END_STAGE_1)) {
                    MessageParts messageParts = ExtractEndMessage(result, ServiceStage.SOCIETY_IDENTIFICATION, ServiceStage.DOCUMENT_COLLECTION, service);
                    result = messageParts.getText();
                }
            }
            case DOCUMENT_COLLECTION -> {
                var stageContexts = stageContextRepository.findByServiceAndTargetStage(service, ServiceStage.DOCUMENT_COLLECTION).stream()
                .map(StageContextEntity::getContext)
                .toList();

                result = clientVirtualAssistantService.respondToUserMessage(aiContentBuilder.build(), user, ServiceStage.DOCUMENT_COLLECTION.getAgentId(),
                        "\nContexto de la conversación:\n" +
                                "\t- Nombre del usuario: " + user.getSuitableName() + ".\n" +
                                "\t- Fecha y Hora actual: " + java.time.LocalDateTime.now() + ".\n" +
                                "\t- Fecha y Hora de la última interacción: " +
                                (user.getLastInteractionDate() == null ? "Nunca" : user.getLastInteractionDate()) + ".\n" +
                                String.join("\\n", stageContexts));

                if (result.contains(END_STAGE_1)) {
                    MessageParts messageParts = ExtractEndMessage(result, ServiceStage.DOCUMENT_COLLECTION, ServiceStage.COMPANY_INCORPORATION, service);
                    result = messageParts.getText();

                    //Generamos el borrador de alta constitutiva
                    String draft = clientVirtualAssistantService.generateCompanyIncorporationDraft(messageParts.getJson(), user);
                    log.info("Borrador generado: {}", draft);
                    log.info("Borrador enviado por correo al usuario: {}", user.getEmail());

                    // Creamos un evento en el calendario
                    GoogleCalendarClient calendarClient = new GoogleCalendarClient("Temis Application");
                    String startDateTime = java.time.LocalDateTime.now().plusDays(1).toString();
                    String endDateTime = java.time.LocalDateTime.now().plusDays(1).plusHours(1).toString();

                    Event event = calendarClient.createEvent(
                            "primary",
                            "Revisión del Borrador de Alta Constitutiva",
                            "Revisar el borrador de alta constitutiva:\n" + draft,
                            "Virtual (Zoom/Google Meet)",
                            startDateTime,
                            endDateTime,
                            new String[]{user.getEmail()}
                    );

                    log.info("Evento creado en el calendario: {}", event.getHtmlLink());
                }
            }
            default -> throw new Exception("Estado " + service.getServiceStage().name() + " es inválido.");
        }

        //Este código se encarga de construir las respuestas al usuario. Tiene que ser ejecutado por cada ejecución de este método o no va a responder nada.
        var sentences = TextUtils.splitIntoSentences(result);
        for (String sentence : sentences) {
            responseBuilder.addContent(sentence);
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
            throw new JSONNotFoundException("Se intentó finalizar la etapa " + current.name() +
                    " del agente pero no se encontró un JSON válido");
        }

        return new MessageParts(result, extractedJson);
    }
}