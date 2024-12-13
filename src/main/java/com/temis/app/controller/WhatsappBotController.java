package com.temis.app.controller;

import java.io.IOException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.temis.app.model.DocumentSummarizeDTO;
import com.temis.app.service.SummarizeService;
import com.temis.app.service.ClientVirtualAssistantService;

@RestController
@RequestMapping("/whatsapp-bot")
@Slf4j
public class WhatsappBotController {

    @Autowired
    private SummarizeService summarizeService;

    @Autowired
    private FirstContactState firstContactState;

    @Autowired
    private TwilioConfigProperties twilioConfigProperties;

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
    public void receiveWhatsAppMessage(@RequestParam Map<String, String> requestBody) throws Exception {
        String userMessage = requestBody.get("Body");
        String phoneNumber = requestBody.get("From");
        String nickName = requestBody.get("ProfileName");
        String SmsMessageSid = requestBody.get("SmsMessageSid");

        log.info("WhatsappBotController twilio: {}", requestBody);

        Twilio.init(twilioConfigProperties.accountSid(), twilioConfigProperties.authToken());

        var messageContextBuilder = MessageContextEntity.builder()
                .messageId("twilio:" + SmsMessageSid)
                .phoneNumber(phoneNumber.replace("whatsapp:", ""))
                .nickName(nickName)
                .body(userMessage)
                .messageSource(MessageSource.TWILIO)
                .request(new HashMap<>(requestBody));


        if ("1".equals(requestBody.get("NumMedia"))) {

            messageContextBuilder.mediaUrl(requestBody.get("MediaUrl0")).mediaContentType(requestBody.get("MediaContentType0"));

        }

        var response = firstContactState.Evaluate(messageContextBuilder.build());

        log.info("Response Generated twilio: {}", new Gson().toJson(response));

        List<String> sentences = TextUtils.splitIntoSentences(response.getBody());

        for (int i = 0; i < sentences.size(); i++)  {
            var sentence = sentences.get(i);

            var message = Message.creator(
                    new PhoneNumber("whatsapp:" + response.getPhoneNumber()),
                    new PhoneNumber(twilioConfigProperties.phoneNumber()),
                    sentence
            );
            if (response.getMediaURL() != null && i == 0) {
                message.setMediaUrl(response.getMediaURL());
            }

            message.create();
            Thread.sleep(1000);
        }

        /*if (response.getQuickActions() != null && !response.getQuickActions().isEmpty()) {
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
        }*/
    }
}