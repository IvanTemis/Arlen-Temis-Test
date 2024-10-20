package com.temis.app.controller;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.temis.app.model.DocumentSummarizeDTO;
import com.temis.app.service.SummarizeService;
import com.temis.app.service.VirtualAssistantService;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;
import com.twilio.twiml.TwiMLException;

@RestController
@RequestMapping("/whatsapp-bot")
public class WhatsappBotController {

    @Autowired
    private SummarizeService summarizeService;
    @Autowired
    private VirtualAssistantService virtualAssistantService;

    Logger logger = LoggerFactory.getLogger(WhatsappBotController.class);

    @GetMapping("/ping")
    public String get(){
        return "Hola Mundo";
    }

    @GetMapping("/summarizeTest")
    public String getSummarizeTest() throws IOException {
        DocumentSummarizeDTO summary = summarizeService.getSummarizeFromDocument();
        return summary.getSummarize();
    }

    //Se usa @RequestParam porque @RequestBody causa conflictos de formato con Twilio
    @PostMapping("/webhook")
    public void receiveWhatsAppMessage(@RequestParam Map<String, String> requestBody) throws IOException, TwiMLException {
        String userMessage = requestBody.get("Body");
        String phoneNumber = requestBody.get("From");

        logger.info("WhatsappBotController: {}", requestBody);

        virtualAssistantService.respondToUserMessage(phoneNumber, userMessage);
    }
}