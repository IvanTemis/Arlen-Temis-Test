package com.temis.app.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.temis.app.config.properties.WhatsappApiConfigProperties;
import com.temis.app.converter.JsonConverter;
import com.temis.app.exceptions.AccessForbiddenException;
import com.temis.app.repository.MessageContextRepository;
import com.whatsapp.api.domain.messages.ImageMessage;
import com.whatsapp.api.domain.messages.TextMessage;
import com.whatsapp.api.domain.webhook.WebHook;
import com.google.gson.Gson;
import com.temis.app.config.properties.TwilioConfigProperties;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.model.MessageSource;
import com.temis.app.state.FirstContactState;
import com.temis.app.utils.TextUtils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.whatsapp.api.domain.webhook.type.FieldType;
import com.whatsapp.api.impl.WhatsappBusinessCloudApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.temis.app.model.DocumentSummarizeDTO;
import com.temis.app.service.SummarizeService;
import com.temis.app.service.VirtualAssistantService;

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
    private WhatsappBusinessCloudApi whatsappBusinessCloudApi;
    @Autowired
    private WhatsappApiConfigProperties whatsappApiConfigProperties;

    @Autowired
    private MessageContextRepository messageContextRepository;

    @GetMapping("/ping")
    public String get() {
        return "Hola Mundo";
    }

    @GetMapping("/summarizeTest")
    public String getSummarizeTest() throws IOException {
        DocumentSummarizeDTO summary = summarizeService.getSummarizeFromDocument();
        return summary.getSummarize();
    }

    @GetMapping(value = "/webhook-meta")
    public String GetMetaChallenge(
            @RequestParam("hub.mode") String hubMode,
            @RequestParam("hub.challenge") String hubChallenge,
            @RequestParam("hub.verify_token") String hubVerifyToken) {

        log.info("WhatsappBotController Get Meta: {} {} {}", hubMode, hubChallenge, hubVerifyToken);

        if(!whatsappApiConfigProperties.verifyToken().equals(hubVerifyToken)){
            log.warn("Verify Token doesn't match.");
            throw new AccessForbiddenException();
        }

        return hubChallenge;
    }

    @PostMapping(value = "/webhook-meta")
    public void PostMetaMessage(@RequestBody String requestBody) throws Exception {
        var webhookEvent = WebHook.constructEvent(requestBody);

        log.info("WhatsappBotController Post meta: {}", requestBody);

        var change = webhookEvent.entry().get(0).changes().get(0);

        if(change.field() != FieldType.MESSAGES) return;

        var value = change.value();

        //Meta nos envía un evento por el cambio de estado de los mensajes en value.statuses(), así que tenemos que confirmar que no sea eso
        if(!value.messagingProduct().equals("whatsapp") || value.statuses() != null) return;

        var message = value.messages().get(0);

        var messageId = "meta:" + message.id();
        //Tenemos que checar si ya lo estamos procesando porque meta reenvía el mensaje si no has respondido con 200 (OK)
        //Así que si nos estamos tomando un rato en procesarlo puede que nos reenvíe el mensaje
        var result = messageContextRepository.findByMessageId(messageId);

        if(!result.isEmpty()) return;

        var phoneNumber = message.from();
        var nickName = message.contacts() != null ? message.contacts().get(0).profile().name() : "UNKNOWN";

        var messageContextBuilder = MessageContextEntity.builder()
                .messageId(messageId)
                .phoneNumber("+" + phoneNumber)
                .nickName(nickName)
                .messageSource(MessageSource.META)
                .request(new JsonConverter().convertToEntityAttribute(requestBody));

        switch (message.type()){
            case TEXT -> messageContextBuilder.body(message.text().body());
            case IMAGE -> {
                var image = message.image();
                messageContextBuilder
                        .body(image.caption() == null ? "" : image.caption())
                        .mediaContentType(image.mimeType())
                        .mediaUrl(whatsappBusinessCloudApi.retrieveMediaUrl(image.id()).url());
            }
            case DOCUMENT -> {
                var document = message.document();
                messageContextBuilder
                        .body(document.caption() == null ? "" : document.caption())
                        .mediaContentType(document.mimeType())
                        .mediaUrl(whatsappBusinessCloudApi.retrieveMediaUrl(document.id()).url());
            }
            default -> {
                return;
            }
        }

        var response = firstContactState.Evaluate(messageContextBuilder.build());

        log.info("Response Generated meta: {}", new Gson().toJson(response));

        List<String> sentences = TextUtils.splitIntoSentences(response.getBody());

        for (int i = 0; i < sentences.size(); i++)  {
            var sentence = sentences.get(i);

            var whappMessageBuilder = com.whatsapp.api.domain.messages.Message.MessageBuilder.builder()
                    .setTo(response.getPhoneNumber().replace("+", ""));

            com.whatsapp.api.domain.messages.Message whappMessage;

            if (response.getMediaURL() != null && i == 0) {
                whappMessage = whappMessageBuilder.buildImageMessage(new ImageMessage()
                        .setCaption(sentence)
                        .setLink(response.getMediaURL().toString())
                );
            }
            else {
                whappMessage = whappMessageBuilder.buildTextMessage(
                        new TextMessage()
                                .setBody(sentence)
                                .setPreviewUrl(false)
                );
            }

            var whappMessageResponse = whatsappBusinessCloudApi.sendMessage(whatsappApiConfigProperties.phoneNumberId(), whappMessage);
        }
    }

    @PostMapping("/webhook-twilio")
    public void receiveTwilioMessage(@RequestParam Map<String, String> requestBody) throws Exception {
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


        if ("1".equals(requestBody.get("NumMedia"))) { //TODO: Checar https://www.twilio.com/docs/messaging/api/media-resource#fetch-a-media-resource

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