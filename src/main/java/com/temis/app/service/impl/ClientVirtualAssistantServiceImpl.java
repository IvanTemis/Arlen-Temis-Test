package com.temis.app.service.impl;

import com.google.api.services.calendar.model.Event;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.temis.app.client.ChatAIClient;
import com.temis.app.client.GoogleCalendarClient;
import com.temis.app.entity.*;
import com.temis.app.manager.AgentManager;
import com.temis.app.repository.VertexAiContentRepository;
import com.temis.app.utils.VertexAIUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.temis.app.service.ClientVirtualAssistantService;

@Service
@Slf4j
public class ClientVirtualAssistantServiceImpl implements ClientVirtualAssistantService {

    @Autowired
    AgentManager agentManager;

    @Autowired
    VertexAiContentRepository vertexAiContextRepository;

    @Autowired
    private DraftEmailService draftEmailService;

    @Override
    public String respondToUserMessage(Content content, UserEntity user, String agentId, String context) throws Exception {

        var contexts = vertexAiContextRepository.findByUserEntityAndAgentIdOrderByCreatedDateAsc(user, agentId);

        var history = VertexAIUtils.VertexAiContentEntityToContent(contexts);

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, content, agentId));

        ChatAIClient chatAIClient = agentManager.getAgent(agentId);
        var response = chatAIClient.sendMessage(content, history, context);

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, ResponseHandler.getContent(response), agentId));

        return ResponseHandler.getText(response);
    }


    //TODO: Este es el mejor lugar para esta lógica?
    @Override
    public String generateCompanyIncorporationDraft(String inputJson, UserEntity user) throws Exception {
        log.info("Generando el borrador para el usuario: {}", user.getSuitableName());
        String agentId = "company-incorporation-agent";
        ChatAIClient chatAIClient = agentManager.getAgent(agentId);

        Content content = VertexAIUtils.createTextContent(
                "Generar borrador para alta constitutiva basado en JSON:\n" + inputJson
        );

        StringBuilder draftBuilder = new StringBuilder();

        var responseStream = chatAIClient.startStreaming(content,
                "Por favor genera un borrador para la alta constitutiva de una empresa utilizando el siguiente contexto:\n" +
                        inputJson + "\n\n" +
                        "Asegúrate de incluir la información relevante como el tipo de sociedad, objeto social, socios, ubicación y denominaciones."
        );

        responseStream.stream().forEach(response -> {
            draftBuilder.append(ResponseHandler.getText(response));
        });

        String draftText = draftBuilder.toString();
        log.info("Borrador generado: {}", draftText);

        draftEmailService.sendDraftByEmail(inputJson, draftText, user.getEmail());
        log.info("Borrador enviado por correo al usuario: {}", user.getEmail());

        /*//TODO Que esta madre jale, o que al menos no tumbe todo el proceso
        //Creamos un evento en el calendario
        GoogleCalendarClient calendarClient = new GoogleCalendarClient("Temis Application");
        String startDateTime = java.time.LocalDateTime.now().plusDays(1).toString();
        String endDateTime = java.time.LocalDateTime.now().plusDays(1).plusHours(1).toString();

        String[] additionalAttendees = {
                "ivan@temislegal.ai",
                "alex@temislegal.ai",
                "diego@temislegal.ai",
                "gabriel@temislegal.ai"
        };

        Event event = calendarClient.createEvent(
                "ivan.cantu.garcia@gmail.com",
                "Revisión del Borrador de Alta Constitutiva",
                "Revisar el borrador de alta constitutiva",
                "Virtual (Zoom/Google Meet)",
                startDateTime,
                endDateTime,
                additionalAttendees
        );

        log.info("Evento creado en el calendario: {}", event.getHtmlLink());*/

        return draftText;
    }
}