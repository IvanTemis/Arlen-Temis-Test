package com.temis.app.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.temis.app.client.VertexAIClient;
import com.temis.app.client.CloudStorageClient;
import com.temis.app.config.properties.CloudConfigProperties;
import com.temis.app.entity.*;
import com.temis.app.manager.AgentManager;
import com.temis.app.repository.VertexAiContentRepository;
import com.temis.app.utils.TemplateSelector;
import com.temis.app.utils.VertexAIUtils;
import com.temis.app.utils.WordDocumentFormatter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.temis.app.service.ClientVirtualAssistantService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

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
    private DraftEmailService draftEmailService;

    @Override
    public String respondToUserMessage(MessageContextEntity messageContext, UserEntity user, String agentId) throws Exception {

        var content = messageContext.toVertexAiContent();

        var response = agentManager.sendMessageToAgentAsUser(agentId, user, content, new HashMap<>(){{
            put("messageContext", messageContext);
        }});

        return response;
    }


    //TODO: Este es el mejor lugar para esta lógica?
        @Override
        public String generateCompanyIncorporationDraft(String inputJson, UserEntity user) throws Exception {
        
        // Parsear el JSON para extraer el campo "codigo_sociedad"
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inputJson);
        String codigoSociedad = rootNode.path("datos_generales").path("codigo_sociedad").asText();

        // Seleccionar dinámicamente la URI del template según el código de sociedad
        String templateUri = TemplateSelector.getTemplatePath(codigoSociedad, cloudConfigProperties);

        // Leer el template como bytes desde el bucket
        byte[] templateBytes = cloudStorageClient.ReadFileBytes(templateUri);

        // Generar el documento formateado usando el JSON proporcionado
        WordDocumentFormatter formatter = new WordDocumentFormatter();
        String cleanJson = inputJson.replace("\"+\"", ""); // Mantiene la lógica actual si es necesaria
        byte[] formattedDocument = formatter.formatDocument(templateBytes, cleanJson);

        // Para pruebas locales: guardar el documento generado
       //Files.write(Paths.get("documento_generado.docx"), formattedDocument);
        
        // Se puede enviar el documento por email, etc.
         draftEmailService.sendDraftByEmailWithAttachment(inputJson, formattedDocument, "Borrador_Constitutiva.docx", user.getEmail());

        log.info("Documento generado y enviado exitosamente a {}", user.getEmail());
        return "Documento generado y enviado exitosamente.";
        }
}