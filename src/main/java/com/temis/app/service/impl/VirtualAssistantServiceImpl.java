package com.temis.app.service.impl;

import org.springframework.stereotype.Service;

import com.temis.app.service.VirtualAssistantService;

@Service
public class VirtualAssistantServiceImpl implements VirtualAssistantService {

    @Override
    public String respondToUserMessage(String userMessage) {
        
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

        return response;
    }
}