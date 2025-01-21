package com.temis.app.service.impl;

import com.google.cloud.tasks.v2.HttpMethod;
import com.temis.app.client.CloudTaskClient;
import com.temis.app.dto.ProcessMessagesRequest;
import com.temis.app.service.MessageProcessingService;
import com.temis.app.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Slf4j
@Service
@Profile("gcloud-run")
public class CloudTasksSchedulerServiceImpl implements SchedulerService {
    private final ConcurrentHashMap<String, String> userTasks = new ConcurrentHashMap<>();

    @Autowired
    private CloudTaskClient cloudTaskClient;

    @Override
    public void ScheduleMessageProcessing(String phoneNumber) {
        log.info("Actualizando scheduler para {}.", phoneNumber);

        try{
            if (userTasks.contains(phoneNumber)) {
                cloudTaskClient.DeleteTask(userTasks.get(phoneNumber));
            }
        }
        catch (Exception e) {
            log.error("Error al cancelar procesamiento de mensajes para " + phoneNumber, e);
        }


        try{
            var task = cloudTaskClient.CreateTask("message-response-queue", "/cloudTasks/processMessages", HttpMethod.POST, new ProcessMessagesRequest(phoneNumber), 10L);

            userTasks.put(phoneNumber, task.getName());
        } catch (Exception e) {
            log.error("Error al programar el procesamiento de mensajes para " + phoneNumber, e);
        }
    }

    @Override
    public void shutdown() {

    }
}