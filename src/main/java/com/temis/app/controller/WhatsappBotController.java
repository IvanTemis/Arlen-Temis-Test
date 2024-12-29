package com.temis.app.controller;

import com.temis.app.entity.MessageContentEntity;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.model.MessageSource;
import com.temis.app.repository.MessageContentRepository;
import com.temis.app.repository.MessageContextRepository;
import com.temis.app.service.MessageProcessingService;
import com.temis.app.service.MessageService;
import com.temis.app.state.FirstContactState;
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
    private MessageProcessingService messageProcessingService;

    @Autowired
    private MessageContextRepository messageContextRepository;
    private MessageContentRepository messageContentRepository;

    @PostMapping("/webhook")
    public void receiveWhatsAppMessage(@RequestParam Map<String, String> requestBody) {
        String userMessage = requestBody.get("Body");
        String phoneNumber = requestBody.get("From").replace("whatsapp:", "");
        String nickName = requestBody.get("ProfileName");
        String SmsMessageSid = requestBody.get("SmsMessageSid");

        if (userMessage == null || phoneNumber == null || SmsMessageSid == null) {
            log.error("Campos obligatorios faltantes en la solicitud: {}", requestBody);
            throw new IllegalArgumentException("Solicitud incompleta: falta Body, From o SmsMessageSid");
        }

        log.info("Mensaje recibido: {}", requestBody);

        var context = messageContextRepository.findFirstByPhoneNumberAndIsActiveTrueOrderByCreateDateAsc(phoneNumber);

        if (context == null) {
            context = MessageContextEntity.builder()
                    .phoneNumber(phoneNumber.replace("whatsapp:", ""))
                    .nickName(nickName)
                    .messageSource(MessageSource.TWILIO)
                    .build();


            messageContextRepository.save(context);
        }

        var contentBuilder = MessageContentEntity.builder()
                .context(context)
                .messageId("twilio:" + SmsMessageSid)
                .body(userMessage)
                .request(new HashMap<>(requestBody));

        if ("1".equals(requestBody.get("NumMedia"))) {
            contentBuilder
                    .mediaUrl(requestBody.get("MediaUrl0"))
                    .mediaContentType(requestBody.get("MediaContentType0"));

        }

        messageContentRepository.save(contentBuilder.build());
        messageProcessingService.scheduleMessageProcessing(phoneNumber);
    }
}