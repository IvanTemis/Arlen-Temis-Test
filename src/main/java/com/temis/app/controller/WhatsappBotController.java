package com.temis.app.controller;

import java.io.IOException;
import java.util.Map;

import com.temis.app.config.properties.TwilioConfigProperties;
import com.temis.app.model.MessageHolderObject;
import com.temis.app.state.FirstContactState;
import com.twilio.Twilio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.temis.app.model.DocumentSummarizeDTO;
import com.temis.app.model.File;
import com.temis.app.service.SummarizeService;
import com.temis.app.service.VirtualAssistantService;
import com.twilio.twiml.TwiMLException;

@RestController
@RequestMapping("/whatsapp-bot")
public class WhatsappBotController {

    @Autowired
    private SummarizeService summarizeService;
    @Autowired
    private VirtualAssistantService virtualAssistantService;

    @Autowired
    private FirstContactState firstContactState;
    @Autowired
    private TwilioConfigProperties twilioConfigProperties;
    @Autowired
    private RestTemplate restTemplate;

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
        String nickName = requestBody.get("ProfileName");

        logger.info("WhatsappBotController: {}", requestBody);

        /*if ("1".equals(requestBody.get("NumMedia"))) {
            String mediaUrl = requestBody.get("MediaUrl0");
            String mediaType = requestBody.get("MediaContentType0");
            String profileName = requestBody.get("ProfileName");
            String messageId = requestBody.get("MessageSid");

            byte[] fileBytes = restTemplate.getForObject(mediaUrl, byte[].class);

            File file = File.builder()
                    .id(messageId)
                    .source("WhatsApp")
                    .mediaUrl(mediaUrl)
                    .mediaType(mediaType)
                    .senderInfo(phoneNumber)
                    .profileName(profileName)
                    .messageId(messageId)
                    .build();

            logger.info("Received file: {}", file);

        }*/

        var response = firstContactState.Evaluate(new MessageHolderObject(phoneNumber.replace("whatsapp:", ""), nickName, requestBody));

        //TODO: Reemplazar con una abstracción a una interfaz común
        Twilio.init(twilioConfigProperties.accountSid(), twilioConfigProperties.authToken());
        com.twilio.rest.api.v2010.account.Message.creator(
                new com.twilio.type.PhoneNumber(response.getPhoneNumber()),
                new com.twilio.type.PhoneNumber(twilioConfigProperties.phoneNumber()),
                response.getBody()).create();

    }
}