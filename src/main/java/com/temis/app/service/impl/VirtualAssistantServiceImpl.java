package com.temis.app.service.impl;

import com.temis.app.config.properties.WhatsappApiConfigProperties;
import com.whatsapp.api.domain.messages.Message;
import com.whatsapp.api.domain.messages.TextMessage;
import com.whatsapp.api.impl.WhatsappBusinessCloudApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.temis.app.service.VirtualAssistantService;

@Service
public class VirtualAssistantServiceImpl implements VirtualAssistantService {

    @Autowired
    private WhatsappBusinessCloudApi whatsappBusinessCloudApi;
    @Autowired
    private WhatsappApiConfigProperties whatsappApiConfigProperties;

    @Override
    public void respondToUserMessage(String phoneNumber, String userMessage) {
        
        String response;

        if (userMessage == null || userMessage.trim().isEmpty()) {
            response = "Lo siento, no entendí tu mensaje. ¿Puedes repetirlo?";
        } else if (userMessage.toLowerCase().contains("hola")) {
            response = "¡Hola! ¿En qué puedo ayudarte hoy?";
        } else if (userMessage.toLowerCase().contains("adiós")) {
            response = "¡Adiós! Espero haberte sido de ayuda.";
        } else if (userMessage.toLowerCase().contains("cómo estás")) {
            response = "¡Estoy aquí para ayudarte! ¿Qué necesitas?";
        } else {
            
            response = "Gracias por tu mensaje. Aún estoy aprendiendo, pero haré lo posible para ayudarte.";
        }

        var message = Message.MessageBuilder.builder()
                        .setTo(phoneNumber)
                        .buildTextMessage(new TextMessage()
                                .setBody(response)
                                .setPreviewUrl(false));

        whatsappBusinessCloudApi.sendMessage(whatsappApiConfigProperties.phoneNumberId(), message);
    }
}