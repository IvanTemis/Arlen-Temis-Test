package com.temis.app.service.impl;

import org.springframework.stereotype.Service;

import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.service.MessagePlatformService;

@Service
public class WhatsappBusinessMessageService implements MessagePlatformService {

    @Override
    public void sendMessage(MessageResponseEntity response) {
        //TO-DO ACÁ LO MISMO QUE TWILIO PARA WHATSAPP
        throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
    }
    
}
