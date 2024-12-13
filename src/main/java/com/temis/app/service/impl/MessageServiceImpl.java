package com.temis.app.service.impl;

import com.temis.app.config.properties.TwilioConfigProperties;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.service.MessageService;
import com.temis.app.utils.TextUtils;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    private TwilioConfigProperties twilioConfigProperties;

    public MessageServiceImpl() {
        
        Twilio.init(twilioConfigProperties.accountSid(), twilioConfigProperties.authToken());
    }

    @Override
    public void sendResponseToUser(MessageResponseEntity response) {
        List<String> sentences = TextUtils.splitIntoSentences(response.getBody());
        for (int i = 0; i < sentences.size(); i++) {
            var sentence = sentences.get(i);

            try {
                var message = Message.creator(
                        new PhoneNumber("whatsapp:" + response.getPhoneNumber()),
                        new PhoneNumber(twilioConfigProperties.phoneNumber()),
                        sentence
                );

                if (response.getMediaURL() != null && i == 0) {
                    message.setMediaUrl(response.getMediaURL());
                }

                message.create();
                log.info("Mensaje enviado a {}: {}", response.getPhoneNumber(), sentence);

                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("Error al enviar mensaje a {}: {}", response.getPhoneNumber(), e.getMessage(), e);
            }
        }
    }
}