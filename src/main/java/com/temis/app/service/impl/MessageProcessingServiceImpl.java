package com.temis.app.service.impl;

import com.temis.app.entity.MessageContextContentEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.repository.MessageContextRepository;
import com.temis.app.service.MessageProcessingService;
import com.temis.app.service.MessageService;
import com.temis.app.state.FirstContactState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.concurrent.*;

@Service
@Slf4j
public class MessageProcessingServiceImpl implements MessageProcessingService {
    @Autowired
    private FirstContactState firstContactState;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageContextRepository messageContextRepository;

    @Override
    public void ProcessAccumulatedMessages(String phoneNumber) throws Exception {

        //TODO: Qué pasa si a 2 números diferentes (Twilio/Meta) del mismo número??
        var context = messageContextRepository.findFirstByPhoneNumberAndIsActiveTrueOrderByCreatedDateAsc(phoneNumber);

        if(context == null){
            log.warn("Contexto nulo para llamada de procecamiento de mensajes del número {}", phoneNumber);
            return;
        }

        log.info("Procesando {} mensaje/s acumulados de {}: {}", context.getMessageContents().size(), phoneNumber, context.getContentAsDebugString());
    
        MessageResponseEntity response = firstContactState.Evaluate(context);
    
        log.info("Se generaron {} respuestas para {}: {}", response.getResponseContents().size(), phoneNumber, response.getContentAsDebugString());
    
        messageService.sendResponseToUser(response);
    }
}