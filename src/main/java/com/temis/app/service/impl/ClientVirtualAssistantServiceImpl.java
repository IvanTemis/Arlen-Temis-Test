package com.temis.app.service.impl;

import com.google.api.services.calendar.model.Event;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.temis.app.client.ChatAIClient;
import com.temis.app.client.CloudStorageClient;
import com.temis.app.config.properties.CloudConfigProperties;
import com.temis.app.client.GoogleCalendarClient;
import com.temis.app.entity.*;
import com.temis.app.manager.AgentManager;
import com.temis.app.repository.VertexAiContentRepository;
import com.temis.app.utils.VertexAIUtils;
import com.temis.app.utils.WordDocumentFormatter;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.temis.app.service.ClientVirtualAssistantService;

@Service
@Slf4j
public class ClientVirtualAssistantServiceImpl implements ClientVirtualAssistantService {

    @Autowired
    AgentManager agentManager;
    
    @Autowired
    private CloudConfigProperties cloudConfigProperties;

    @Autowired
    private CloudStorageClient cloudStorageClient;

    @Autowired
    VertexAiContentRepository vertexAiContextRepository;

    @Autowired
    private DraftEmailService draftEmailService;

    @Override
    public String respondToUserMessage(Content content, UserEntity user, String agentId, String context) throws Exception {

        var contexts = vertexAiContextRepository.findByUserEntityOrderByCreatedDateAsc(user);

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
        // Ruta del archivo en el bucket
        String templateUri = "gs://" + cloudConfigProperties.getStorage().getBucketName() + "/drafts/machote.docx";

        // Leer el machote como bytes desde el bucket
        byte[] templateBytes = cloudStorageClient.ReadFileBytes(templateUri);

        // Generar el documento formateado usando el JSON proporcionado
        WordDocumentFormatter formatter = new WordDocumentFormatter();
        byte[] formattedDocument = formatter.createFormattedDocument(templateBytes, inputJson);


      draftEmailService.sendDraftByEmailWithAttachment(inputJson, formattedDocument, "Borrador_Constitutiva.docx", user.getEmail());

        log.info("Documento generado y enviado exitosamente a {}", user.getEmail());

        return "Documento generado y enviado exitosamente.";

    }
}