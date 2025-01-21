package com.temis.app.controller;

import com.temis.app.config.properties.WhatsappApiConfigProperties;
import com.temis.app.converter.JsonConverter;
import com.temis.app.entity.MessageContextContentEntity;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.exceptions.AccessForbiddenException;
import com.temis.app.model.MessageSource;
import com.temis.app.repository.MessageContextContentRepository;
import com.temis.app.repository.MessageContextRepository;
import com.temis.app.service.MessageService;
import com.temis.app.service.SchedulerService;
import com.temis.app.state.FirstContactState;
import com.whatsapp.api.domain.webhook.WebHook;
import com.whatsapp.api.domain.webhook.type.FieldType;
import com.whatsapp.api.impl.WhatsappBusinessCloudApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/whatsapp-bot")
@Slf4j
public class WhatsappBotController {

    @Autowired
    private FirstContactState firstContactState;

    @Autowired
    private MessageService messageService;
    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private WhatsappBusinessCloudApi whatsappBusinessCloudApi;
    @Autowired
    private WhatsappApiConfigProperties whatsappApiConfigProperties;

    @Autowired
    private MessageContextRepository messageContextRepository;

    @Autowired
    private MessageContextContentRepository messageContextContentRepository;

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
        var result = messageContextContentRepository.findByMessageId(messageId);

        if(!result.isEmpty()) return;

        var phoneNumber = "+" + message.from();
        var nickName = message.contacts() != null ? message.contacts().get(0).profile().name() : "UNKNOWN";

        var context = messageContextRepository.findFirstByPhoneNumberAndMessageSourceAndIsActiveTrueOrderByCreatedDateAsc(phoneNumber, MessageSource.META);

        if (context == null) {
            context = MessageContextEntity.builder()
                    .phoneNumber(phoneNumber)
                    .nickName(nickName)
                    .messageSource(MessageSource.META)
                    .isActive(true)
                    .build();


            messageContextRepository.save(context);
        }

        var contentBuilder = MessageContextContentEntity.builder()
                .context(context)
                .messageId(messageId)
                .request(new JsonConverter().convertToEntityAttribute(requestBody));

        switch (message.type()){
            case TEXT -> contentBuilder.body(message.text().body());
            case IMAGE -> {
                var image = message.image();
                contentBuilder
                        .body(image.caption() == null ? "" : image.caption())
                        .mediaContentType(image.mimeType())
                        .mediaUrl(whatsappBusinessCloudApi.retrieveMediaUrl(image.id()).url());
            }
            case DOCUMENT -> {
                var document = message.document();
                contentBuilder
                        .body(document.caption() == null ? "" : document.caption())
                        .mediaContentType(document.mimeType())
                        .mediaUrl(whatsappBusinessCloudApi.retrieveMediaUrl(document.id()).url());
            }
            default -> {
                log.warn("El Webhook de whatsapp envió un mensaje desconocido de tipo {}: {}", message.type(), requestBody);
                return;
            }
        }

        messageContextContentRepository.save(contentBuilder.build());
        schedulerService.ScheduleMessageProcessing(phoneNumber);
    }

    @PostMapping("/webhook-twilio")
    public void receiveTwilioMessage(@RequestParam Map<String, String> requestBody) throws Exception {
        String userMessage = requestBody.get("Body");
        String phoneNumber = requestBody.get("From").replace("whatsapp:", "");
        String nickName = requestBody.get("ProfileName");
        String SmsMessageSid = requestBody.get("SmsMessageSid");

        if (userMessage == null || phoneNumber == null || SmsMessageSid == null) {
            log.error("Campos obligatorios faltantes en la solicitud: {}", requestBody);
            throw new IllegalArgumentException("Solicitud incompleta: falta Body, From o SmsMessageSid");
        }

        log.info("WhatsappBotController twilio: {}", requestBody);

        var context = messageContextRepository.findFirstByPhoneNumberAndMessageSourceAndIsActiveTrueOrderByCreatedDateAsc(phoneNumber, MessageSource.TWILIO);

        if (context == null) {
            context = MessageContextEntity.builder()
                    .phoneNumber(phoneNumber.replace("whatsapp:", ""))
                    .nickName(nickName)
                    .messageSource(MessageSource.TWILIO)
                    .isActive(true)
                    .build();


            messageContextRepository.save(context);
        }

        var contentBuilder = MessageContextContentEntity.builder()
                .context(context)
                .messageId("twilio:" + SmsMessageSid)
                .body(userMessage)
                .request(new HashMap<>(requestBody));

        if ("1".equals(requestBody.get("NumMedia"))) {
            contentBuilder
                    .mediaUrl(requestBody.get("MediaUrl0"))
                    .mediaContentType(requestBody.get("MediaContentType0"));

        }

        messageContextContentRepository.save(contentBuilder.build());
        schedulerService.ScheduleMessageProcessing(phoneNumber);
    }
}