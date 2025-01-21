package com.temis.app.controller;

import com.temis.app.dto.ProcessMessagesRequest;
import com.temis.app.service.MessageProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cloudTasks")
@Slf4j
@Profile("gcloud-run")
public class CloudTasksController {

    @Autowired
    private MessageProcessingService messageProcessingService;

    @PostMapping("/processMessages")
    public void processMessages(@RequestBody ProcessMessagesRequest processMessagesRequest) {

        String phoneNumber = processMessagesRequest.getPhoneNumber();

        try {
            messageProcessingService.ProcessAccumulatedMessages(phoneNumber);
        } catch (Exception e) {
            log.error("Error durante procesamiento de mensajes acumulados para " + phoneNumber, e);
        }
    }
}