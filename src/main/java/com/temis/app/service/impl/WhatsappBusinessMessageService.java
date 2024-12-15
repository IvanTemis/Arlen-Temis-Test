package com.temis.app.service.impl;

import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.service.MessagePlatformService;

public class WhatsappBusinessMessageService implements MessagePlatformService {

    @Override
    public void sendMessage(MessageResponseEntity response) {
        //TO-DO ACÁ LO MISMO QUE TWILIO PARA WHATSAPP
        throw new UnsupportedOperationException("Unimplemented method 'sendMessage'");
    }
    
}
