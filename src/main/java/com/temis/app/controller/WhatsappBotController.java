package com.temis.app.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/ping")
    public String get(){
        return "Hola Mundo";
    }

    @GetMapping("/summarizeTest")
    public String getSummarizeTest() throws IOException {
        DocumentSummarizeDTO summary = summarizeService.getSummarizeFromDocument();
        return summary.getSummarize();
    }

    @PostMapping("/webhook")
    public String receiveWhatsAppMessage(@RequestBody Map<String, String> requestBody) throws IOException, TwiMLException {
        String userMessage = requestBody.get("Body");

        String responseMessage = virtualAssistantService.respondToUserMessage(userMessage);

        Body body = new Body.Builder(responseMessage).build();
        Message message = new Message.Builder().body(body).build();
        MessagingResponse response = new MessagingResponse.Builder().message(message).build();

        return response.toXml();
    }
}