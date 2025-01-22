package com.temis.app.controller;

import com.temis.app.dto.ProcessMessagesRequest;
import com.temis.app.entity.ScheduledProcessEntity;
import com.temis.app.model.ScheduledProcessSchedulerType;
import com.temis.app.model.ScheduledProcessState;
import com.temis.app.model.ScheduledProcessType;
import com.temis.app.repository.MessageContextContentRepository;
import com.temis.app.repository.ScheduledProcessRepository;
import com.temis.app.service.MessageProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Profile("gcloud")
@RestController
@RequestMapping("/cloudTasks")
@Slf4j
public class CloudTasksController {

    @Autowired
    private MessageProcessingService messageProcessingService;


    @Autowired
    private MessageContextContentRepository messageContextContentRepository;

    @Autowired
    private ScheduledProcessRepository scheduledProcessRepository;


    @PostMapping("/processMessages")
    public void processMessages(@RequestHeader("x-cloudtasks-taskname") String taskName, @RequestBody ProcessMessagesRequest processMessagesRequest) {

        var pendingScheduleOptional = scheduledProcessRepository.findByName(taskName);

        if(pendingScheduleOptional.isEmpty()){
            log.error("Se intentó procesar un una tarea que no se encontró en los registros '{}'", taskName);
            return;
        }

        var pendingSchedule = pendingScheduleOptional.get();

        switch (pendingSchedule.getState()){
            case DONE -> {
                log.warn("Se intentó procesar un una tarea ya completada '{}'", taskName);
                return;
            }
            case CANCELLED -> {
                log.warn("Se intentó procesar un una tarea ya cancelada '{}'", taskName);
                return;
            }
            default -> {}
        }

        var messageId = processMessagesRequest.getMessageId();

        var result = messageContextContentRepository.findByMessageId(messageId);

        if(result.isEmpty()){
            log.error("Se intentó procesar un mensaje no existente con ID {}", messageId);
            return;
        }

        var message = result.get();

        var context = message.getContext();

        if(!context.isActive()){
            log.warn("Se intentó procesar un mensaje ya procesado con ID {}", messageId);
            return;
        }

        if(!context.getMessageContents().getLast().getMessageId().equals(messageId)){
            log.info("Saltando el llamado a procesamiento del mensaje '{}' porque no es el mensaje final de la cadena.", messageId);
            return;
        }

        String phoneNumber = processMessagesRequest.getPhoneNumber();

        try {
            messageProcessingService.ProcessAccumulatedMessages(phoneNumber);
        } catch (Exception e) {
            log.error("Error durante procesamiento de mensajes acumulados para " + phoneNumber, e);
        }
        
        pendingSchedule.setState(ScheduledProcessState.DONE);
        scheduledProcessRepository.save(pendingSchedule);
    }
}