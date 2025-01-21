package com.temis.app.service.impl;

import com.temis.app.service.MessageProcessingService;
import com.temis.app.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

@Slf4j
@Profile("!gcloud")
@Service
public class LocalSchedulerServiceImpl implements SchedulerService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<String, ScheduledFuture<?>> userTimers = new ConcurrentHashMap<>();

    @Autowired
    private MessageProcessingService messageProcessingService;

    @PostConstruct
    private void ReschedulePending(){
        //TODO: Re-schedulear los mensajes pendientes cuando se cerró el app?
    }

    @Override
    public void ScheduleMessageProcessing(String phoneNumber) {
        log.info("Actualizando scheduler para {}.", phoneNumber);

        ScheduledFuture<?> previousTimer = userTimers.get(phoneNumber);
        if (previousTimer != null && !previousTimer.isDone()) {
            previousTimer.cancel(false);
        }

        ScheduledFuture<?> timer = scheduler.schedule(() -> {
            try {
                messageProcessingService.ProcessAccumulatedMessages(phoneNumber);
            } catch (Exception e) {
                log.error("Error durante procesamiento de mensajes acumulados para " + phoneNumber, e);
            }
        }, 10, TimeUnit.SECONDS);
        userTimers.put(phoneNumber, timer);
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