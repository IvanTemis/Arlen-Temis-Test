package com.temis.app.service.impl;

import com.temis.app.config.properties.TwilioConfigProperties;
import com.temis.app.entity.MessageResponseContentEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.service.MessagePlatformService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;

@Service
@Slf4j
public class TwilioMessageService implements MessagePlatformService {

    @Autowired
    private TwilioConfigProperties twilioConfigProperties;

    @PostConstruct
    public void initializeTwilio() {
        Twilio.init(twilioConfigProperties.accountSid(), twilioConfigProperties.authToken());
        log.info("Twilio inicializado correctamente.");
    }

    @Override
    public void sendMessage(MessageResponseEntity response) throws InterruptedException {

            if (response.getPhoneNumber() == null || response.getPhoneNumber().isEmpty()) {
                log.error("El número de teléfono es nulo o vacío. No se enviará el mensaje.");
                return;
            }

            for (MessageResponseContentEntity content : response.getResponseContents()) {
                if (content.getBody() == null || content.getBody().isEmpty()) {
                    log.error("El cuerpo del mensaje es nulo o vacío. No se enviará el mensaje a {}.", response.getPhoneNumber());
                    continue;
                }

                if (content.getMediaURL() != null && !isValidUrl(content.getMediaURL())) {
                    log.error("La URL de medios no es válida: {}. No se incluirá en el mensaje a {}.", content.getMediaURL(), response.getPhoneNumber());
                }

                var messageCreator = Message.creator(
                    new PhoneNumber("whatsapp:" + response.getPhoneNumber()),
                    new PhoneNumber(twilioConfigProperties.phoneNumber()),
                    content.getBody()
                );

                if (content.getMediaURL() != null && isValidUrl(content.getMediaURL())) {
                    messageCreator.setMediaUrl(content.getMediaURL());
                }

                messageCreator.create();
                log.info("Mensaje enviado a {}: {} {}", response.getPhoneNumber(), content.getBody(), content.getMediaURL());

                Thread.sleep(2000);
            }
        }

    private boolean isValidUrl(URI uri) {
        try {
            uri.toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}