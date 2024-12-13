package com.temis.app.controller;

import com.temis.app.service.MessageProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/whatsapp-bot")
@Slf4j
public class WhatsappBotController {

    @Autowired
    private MessageProcessingService messageProcessingService;

    @PostMapping("/webhook")
    public void receiveWhatsAppMessage(@RequestParam Map<String, String> requestBody) {
        String userMessage = requestBody.get("Body");
        String phoneNumber = requestBody.get("From").replace("whatsapp:", "");

        log.info("Mensaje recibido de {}: {}", phoneNumber, userMessage);

        messageProcessingService.accumulateMessage(phoneNumber, userMessage);
        messageProcessingService.scheduleMessageProcessing(phoneNumber);
    }
}