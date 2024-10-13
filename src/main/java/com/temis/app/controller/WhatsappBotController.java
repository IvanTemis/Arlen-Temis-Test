package com.temis.app.controller;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.whatsapp.api.domain.webhook.WebHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.temis.app.model.DocumentSummarizeDTO;
import com.temis.app.service.SummarizeService;
import com.temis.app.service.VirtualAssistantService;

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
    public void receiveWhatsAppMessage(@RequestBody String body) throws JsonProcessingException {
        var webhookEvent = WebHook.constructEvent(body);


    }
}