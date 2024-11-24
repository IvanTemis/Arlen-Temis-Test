package com.temis.app.service.impl;

import com.temis.app.config.properties.TwilioConfigProperties;
import com.twilio.Twilio;
import com.twilio.converter.Promoter;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.temis.app.service.VirtualAssistantService;

@Service
public class VirtualAssistantServiceImpl implements VirtualAssistantService {

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
    }
}