package com.temis.app.state.with_service;

import com.google.gson.Gson;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.entity.ServiceEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.exception.JSONNotFoundException;
import com.temis.app.model.ServiceStage;
import com.temis.app.service.ClientVirtualAssistantService;
import com.temis.app.state.with_user.StateWithUserTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Component
public class ClientVirtualAssistantState extends StateWithServiceTemplate {

    static final String END_STAGE = "END_STAGE_1";


    Pattern jsonPattern = Pattern.compile("(\\[*\\{(?:.*|[\\n\\t\\r]*)+?\\}\\]*)",
            Pattern.CASE_INSENSITIVE);

    @Autowired
    private ClientVirtualAssistantService clientVirtualAssistantService;

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

            result = result.replace(END_STAGE, "");
            
            service.setServiceStage(ServiceStage.DOCUMENT_COLLECTION);

            var matcher = jsonPattern.matcher(result);

            if(matcher.find()){

                var json = matcher.group(1);

                log.info("Se finalizó primer etapa con json: {}", json);

                result = result.replace(json, "").replace("```json", "").trim();
            }
            else{
                throw new JSONNotFoundException("Se intentó finalizar la etapa SOCIETY_IDENTIFICATION del agente pero no se encontró un JSON válido");
            }
        }

        responseBuilder.body(result);
    }
}
