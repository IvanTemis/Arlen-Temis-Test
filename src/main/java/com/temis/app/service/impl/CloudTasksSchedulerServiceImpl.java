package com.temis.app.service.impl;

import com.google.cloud.tasks.v2.HttpMethod;
import com.temis.app.client.CloudTaskClient;
import com.temis.app.dto.GenerateDraftRequest;
import com.temis.app.dto.ProcessMessagesRequest;
import com.temis.app.entity.ScheduledProcessEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.model.ScheduledProcessSchedulerType;
import com.temis.app.model.ScheduledProcessState;
import com.temis.app.model.ScheduledProcessType;
import com.temis.app.repository.ScheduledProcessRepository;
import com.temis.app.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

@Slf4j
@Profile("gcloud")
@Service
public class CloudTasksSchedulerServiceImpl implements SchedulerService {
    @Autowired
    private CloudTaskClient cloudTaskClient;

    @Autowired
    private ScheduledProcessRepository scheduledProcessRepository;


    void DeletePendingTask(String parent, ScheduledProcessType scheduledProcessType){
        var pendingSchedules = scheduledProcessRepository.findByParentAndStateAndTypeAndSchedulerTypeOrderByCreatedDateAsc(parent, ScheduledProcessState.PENDING, scheduledProcessType, ScheduledProcessSchedulerType.GCLOUD_TASKS);

        for (ScheduledProcessEntity pendingSchedule : pendingSchedules) {
            try {
                cloudTaskClient.DeleteTask(pendingSchedule.getName());
                log.info("Se canceló la tarea '" + pendingSchedule.getName() + "' de procesamiento de mensajes para " + parent);
                pendingSchedule.setState(ScheduledProcessState.CANCELLED);
            } catch (com.google.api.gax.rpc.NotFoundException e) {
                pendingSchedule.setState(ScheduledProcessState.NOT_FOUND);
                log.warn("No se encontró la tarea '" + pendingSchedule.getName() + "' de procesamiento de mensajes para " + parent);
            } catch (Exception e) {
                pendingSchedule.setState(ScheduledProcessState.ERROR);
                log.error("Error al cancelar tarea '" + pendingSchedule.getName() + "' de procesamiento de mensajes para " + parent, e);
            }
        }
        scheduledProcessRepository.saveAll(pendingSchedules);
    }

    @Override
    public void ScheduleMessageProcessing(String phoneNumber, String messageId) {
        log.info("Actualizando scheduler para {}.", phoneNumber);

        DeletePendingTask(phoneNumber, ScheduledProcessType.MESSAGE_RESPONSE);

        try {
            var task = cloudTaskClient.CreateTask("message-response-queue", messageId, "/cloudTasks/processMessages", HttpMethod.POST, new ProcessMessagesRequest(phoneNumber, messageId), 10L);

            var scheduledProcess = ScheduledProcessEntity.builder()
                    .name(task.getName())
                    .parent(phoneNumber)
                    .state(ScheduledProcessState.PENDING)
                    .type(ScheduledProcessType.MESSAGE_RESPONSE)
                    .schedulerType(ScheduledProcessSchedulerType.GCLOUD_TASKS)
                    .build();

            scheduledProcessRepository.save(scheduledProcess);
        } catch (Exception e) {
            log.error("Error al programar el procesamiento de mensajes para " + phoneNumber, e);
        }
    }

    @Override
    public void ScheduleDraftGeneration(String inputJson, UserEntity user) throws Exception {
        log.info("Generando scheduler para generación de Draft para {}.", user.getSuitableName());

        String phoneNumber = user.getPhoneNumber();

        DeletePendingTask(phoneNumber, ScheduledProcessType.DRAFT_GENERATION);

        try {
            var task = cloudTaskClient.CreateTask("draft-generation-queue", null, "/cloudTasks/generateDraft", HttpMethod.POST, new GenerateDraftRequest(user.getId(), inputJson), 0L);

            var scheduledProcess = ScheduledProcessEntity.builder()
                    .name(task.getName())
                    .parent(phoneNumber)
                    .state(ScheduledProcessState.PENDING)
                    .type(ScheduledProcessType.DRAFT_GENERATION)
                    .schedulerType(ScheduledProcessSchedulerType.GCLOUD_TASKS)
                    .build();

            scheduledProcessRepository.save(scheduledProcess);
        } catch (Exception e) {
            log.error("Error al programar el procesamiento de mensajes para " + phoneNumber, e);
        }
    }

    @Override
    public void shutdown() {

    }
}