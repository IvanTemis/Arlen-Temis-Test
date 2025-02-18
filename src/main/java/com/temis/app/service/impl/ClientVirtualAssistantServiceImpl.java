package com.temis.app.service.impl;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.temis.app.client.VertexAIClient;
import com.temis.app.client.CloudStorageClient;
import com.temis.app.config.properties.CloudConfigProperties;
import com.temis.app.entity.*;
import com.temis.app.manager.AgentManager;
import com.temis.app.repository.VertexAiContentRepository;
import com.temis.app.utils.VertexAIUtils;
import com.temis.app.utils.WordDocumentFormatter;

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
    private CloudConfigProperties cloudConfigProperties;

    @Autowired
    private CloudStorageClient cloudStorageClient;

    @Autowired
    VertexAiContentRepository vertexAiContextRepository;

    @Autowired
    private DraftEmailService draftEmailService;

    @Override
    public String respondToUserMessage(Content content, UserEntity user, String agentId, MessageContextEntity messageContext) throws Exception {

        var contexts = vertexAiContextRepository.findByUserEntityOrderByCreatedDateAsc(user);

        var history = VertexAIUtils.VertexAiContentEntityToContent(contexts);

        vertexAiContextRepository.save(VertexAiContentEntity.fromContent(user, content, agentId));

        var response = agentManager.sendMessageToAgent(agentId, content, history, messageContext);

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
        String cleanJson = inputJson.replace("\"+\"", "");
        byte[] formattedDocument = formatter.formatDocument(templateBytes, cleanJson);

        //INFO - Para pruebas del machote en local.
        //Files.write(Paths.get("documento_generado.docx"), formattedDocument);
     
        draftEmailService.sendDraftByEmailWithAttachment(inputJson, formattedDocument, "Borrador_Constitutiva.docx", user.getEmail());

        log.info("Documento generado y enviado exitosamente a {}", user.getEmail());

        return "Documento generado y enviado exitosamente.";

    }
}