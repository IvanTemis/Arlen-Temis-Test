package com.temis.app.controller;

import com.temis.app.dto.GenerateDraftRequest;
import com.temis.app.dto.ProcessMessagesRequest;
import com.temis.app.entity.ScheduledProcessEntity;
import com.temis.app.model.ScheduledProcessSchedulerType;
import com.temis.app.model.ScheduledProcessState;
import com.temis.app.model.ScheduledProcessType;
import com.temis.app.repository.MessageContextContentRepository;
import com.temis.app.repository.ScheduledProcessRepository;
import com.temis.app.repository.UserRepository;
import com.temis.app.service.ClientVirtualAssistantService;
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
    private ClientVirtualAssistantService clientVirtualAssistantService;


    @Autowired
    private MessageContextContentRepository messageContextContentRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduledProcessRepository scheduledProcessRepository;


    private ScheduledProcessEntity TaskGate(String taskName, ScheduledProcessType scheduledProcessType){
        var pendingScheduleOptional = scheduledProcessRepository.findByNameEndingWithAndTypeAndSchedulerType(taskName, scheduledProcessType, ScheduledProcessSchedulerType.GCLOUD_TASKS);

        if(pendingScheduleOptional.isEmpty()){
            log.error("Se intentó procesar un una tarea que no se encontró en los registros '{}'", taskName);
            return null;
        }

        var pendingSchedule = pendingScheduleOptional.get();

        switch (pendingSchedule.getState()){
            case DONE -> {
                log.warn("Se intentó procesar un una tarea ya completada '{}'", taskName);
                return null;
            }
            case CANCELLED, NOT_FOUND, ERROR -> {
                log.warn("Se intentó procesar un una tarea ya cancelada '{}'", taskName);
                return null;
            }
            case SKIPPED -> {
                log.warn("Se intentó procesar un una tarea ya saltada '{}'", taskName);
                return null;
            }
            default -> {}
        }

        return pendingSchedule;
    }


    @PostMapping("/processMessages")
    public void processMessages(@RequestHeader("x-cloudtasks-taskname") String taskName, @RequestBody ProcessMessagesRequest processMessagesRequest) {
        var pendingSchedule = TaskGate(taskName, ScheduledProcessType.MESSAGE_RESPONSE);
        if(pendingSchedule == null){
            return;
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
            pendingSchedule.setState(ScheduledProcessState.SKIPPED);
            scheduledProcessRepository.save(pendingSchedule);
            return;
        }
        var contents = context.getMessageContents();
        if(!contents.get(contents.size() - 1).getMessageId().equals(messageId)){
            log.info("Saltando el llamado a procesamiento del mensaje '{}' porque no es el mensaje final de la cadena.", messageId);
            pendingSchedule.setState(ScheduledProcessState.SKIPPED);
            scheduledProcessRepository.save(pendingSchedule);
            return;
        }

        String phoneNumber = processMessagesRequest.getPhoneNumber();

        try {
            messageProcessingService.ProcessAccumulatedMessages(phoneNumber);
            pendingSchedule.setState(ScheduledProcessState.DONE);
        } catch (Exception e) {
            pendingSchedule.setState(ScheduledProcessState.PROCESS_ERROR);
            log.error("Error durante procesamiento de mensajes acumulados para " + phoneNumber, e);
        }

        scheduledProcessRepository.save(pendingSchedule);
    }

    @PostMapping("/generateDraft")
    public void generateDraft(@RequestHeader("x-cloudtasks-taskname") String taskName, @RequestBody GenerateDraftRequest generateDraftRequest) {

        var pendingSchedule = TaskGate(taskName, ScheduledProcessType.DRAFT_GENERATION);
        if(pendingSchedule == null){
            return;
        }

        var optionalUser = userRepository.findById(generateDraftRequest.getUserId());

        if(optionalUser.isEmpty()){
            log.error("Se intentó generar draft para usuario inexistente con id '{}'", generateDraftRequest.getUserId());
            return;
        }
        var user = optionalUser.get();
        
        try {
            String draft = clientVirtualAssistantService.generateCompanyIncorporationDraft(generateDraftRequest.getJson(), user);
            pendingSchedule.setState(ScheduledProcessState.DONE);
        } catch (Exception e) {
            pendingSchedule.setState(ScheduledProcessState.PROCESS_ERROR);
            log.error("Error durante la generación del Draft para " + user.getPhoneNumber(), e);
        }

        scheduledProcessRepository.save(pendingSchedule);
    }
}