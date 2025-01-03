package com.temis.app.service.impl;

import com.temis.app.config.properties.WhatsappApiConfigProperties;
import com.temis.app.entity.MessageResponseContentEntity;
import com.whatsapp.api.domain.messages.ImageMessage;
import com.whatsapp.api.domain.messages.TextMessage;
import com.whatsapp.api.impl.WhatsappBusinessCloudApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.service.MessagePlatformService;

@Slf4j
@Service
public class WhatsappBusinessMessageService implements MessagePlatformService {

    @Autowired
    private WhatsappApiConfigProperties whatsappApiConfigProperties;
    @Autowired
    private WhatsappBusinessCloudApi whatsappBusinessCloudApi;

    @Override
    public void sendMessage(MessageResponseEntity response) {
        for (MessageResponseContentEntity content : response.getResponseContents()) {
            var whappMessageBuilder = com.whatsapp.api.domain.messages.Message.MessageBuilder.builder()
                    .setTo(response.getPhoneNumber().replace("+", ""));

            com.whatsapp.api.domain.messages.Message whappMessage;

            if (content.getMediaURL() != null) {
                whappMessage = whappMessageBuilder.buildImageMessage(new ImageMessage()
                        .setCaption(content.getBody())
                        .setLink(content.getMediaURL().toString())
                );
            }
            else {
                whappMessage = whappMessageBuilder.buildTextMessage(
                        new TextMessage()
                                .setBody(content.getBody())
                                .setPreviewUrl(false)
                );
            }

            var whappMessageResponse = whatsappBusinessCloudApi.sendMessage(whatsappApiConfigProperties.phoneNumberId(), whappMessage);
            log.info("Mensaje enviado a {}: {} {}", response.getPhoneNumber(), content.getBody(), content.getMediaURL());
        }
    }
    
}
