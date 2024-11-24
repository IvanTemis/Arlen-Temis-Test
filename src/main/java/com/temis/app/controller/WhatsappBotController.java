package com.temis.app.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.temis.app.config.properties.TwilioConfigProperties;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.model.MessageSource;
import com.temis.app.state.FirstContactState;
import com.temis.app.utils.TextUtils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.temis.app.model.DocumentSummarizeDTO;
import com.temis.app.service.SummarizeService;
import com.temis.app.service.VirtualAssistantService;
import com.twilio.twiml.TwiMLException;

@RestController
@RequestMapping("/whatsapp-bot")
@Slf4j
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

    @GetMapping("/ping")
    public String get() {
        return "Hola Mundo";
    }

    @GetMapping("/summarizeTest")
    public String getSummarizeTest() throws IOException {
        DocumentSummarizeDTO summary = summarizeService.getSummarizeFromDocument();
        return summary.getSummarize();
    }

    @PostMapping("/webhook")
    public void receiveWhatsAppMessage(@RequestParam Map<String, String> requestBody) throws IOException, TwiMLException {
        String userMessage = requestBody.get("Body");
        String phoneNumber = requestBody.get("From");
        String nickName = requestBody.get("ProfileName");

        log.info("WhatsappBotController: {}", requestBody);

        Twilio.init(twilioConfigProperties.accountSid(), twilioConfigProperties.authToken());

        var response = firstContactState.Evaluate(MessageContextEntity.builder()
                .phoneNumber(phoneNumber.replace("whatsapp:", ""))
                .nickName(nickName)
                .body(userMessage)
                .messageSource(MessageSource.TWILIO)
                .request(requestBody).build());

        log.info("Response Generated: {}", new Gson().toJson(response));

        List<String> sentences = TextUtils.splitIntoSentences(response.getBody());

        for (String sentence : sentences) {
            var message = Message.creator(
                    new PhoneNumber("whatsapp:" + response.getPhoneNumber()),
                    new PhoneNumber(twilioConfigProperties.phoneNumber()),
                    sentence
            );

            if (response.getMediaURL() != null) {
                message.setMediaUrl(response.getMediaURL());
            }

            message.create();
        }

        if (response.getQuickActions() != null && !response.getQuickActions().isEmpty()) {
            assert response.getQuickActions().size() == 1;

            var actionMessage = Message.creator(
                    new PhoneNumber("whatsapp:" + response.getPhoneNumber()),
                    new PhoneNumber(twilioConfigProperties.phoneNumber()),
                    ""
            );

            actionMessage.setContentSid("HXfe43ff1faacb77d06dbcd87bc39af681")
                    .setContentVariables(new JSONObject(new HashMap<String, Object>() {
                        {
                            put("body", response.getBody());
                            put("1", response.getQuickActions().get(0));
                        }
                    }).toString());

            actionMessage.create();
        }
    }
}