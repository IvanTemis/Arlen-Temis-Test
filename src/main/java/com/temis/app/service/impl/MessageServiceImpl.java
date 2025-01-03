package com.temis.app.service.impl;

import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.model.MessageSource;
import com.temis.app.service.MessagePlatformService;
import com.temis.app.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Autowired
    private Map<MessageSource, MessagePlatformService> platformServices;

    @Override
    public void sendResponseToUser(MessageResponseEntity response) throws InterruptedException {
        MessageSource source = response.getMessageContextEntity().getMessageSource();

        MessagePlatformService platformService = platformServices.get(source);
        if (platformService == null) {
            log.error("No se encontró un servicio para la fuente de mensajes: {}", source);
            throw new IllegalArgumentException("Fuente de mensajes no soportada: " + source);
        }

        platformService.sendMessage(response);
    }
}