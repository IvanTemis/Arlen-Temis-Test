package com.temis.app.service.impl;

import com.temis.app.config.properties.TwilioConfigProperties;
import com.temis.app.entity.MessageResponseContentEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.service.MessagePlatformService;
import com.temis.app.utils.TextUtils;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

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

            for (MessageResponseContentEntity content : response.getResponseContents()) {

                var message = Message.creator(
                        new PhoneNumber("whatsapp:" + response.getPhoneNumber()),
                        new PhoneNumber(twilioConfigProperties.phoneNumber()),
                        content.getBody()
                );

                if (content.getMediaURL() != null) {
                    message.setMediaUrl(content.getMediaURL());
                }

                message.create();
                log.info("Mensaje enviado a {}: {} {}", response.getPhoneNumber(), content.getBody(), content.getMediaURL());

                Thread.sleep(2000);
            }
    }
}