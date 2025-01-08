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

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<String, ScheduledFuture<?>> userTimers = new ConcurrentHashMap<>();

    @Autowired
    private FirstContactState firstContactState;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageContextRepository messageContextRepository;

    @Override
    public void scheduleMessageProcessing(String phoneNumber) {
        log.info("Actualizando scheduler para {}.", phoneNumber);
        ScheduledFuture<?> previousTimer = userTimers.get(phoneNumber);
        if (previousTimer != null && !previousTimer.isDone()) {
            previousTimer.cancel(false);
        }

        ScheduledFuture<?> timer = scheduler.schedule(() -> {
            try {
                processAccumulatedMessages(phoneNumber);
            } catch (Exception e) {
                log.error("Error durante procesamiento de mensajes acumulados para " + phoneNumber, e);
            }
        }, 10, TimeUnit.SECONDS);
        userTimers.put(phoneNumber, timer);
    }

    private void processAccumulatedMessages(String phoneNumber) throws Exception {

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

    @Override
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}