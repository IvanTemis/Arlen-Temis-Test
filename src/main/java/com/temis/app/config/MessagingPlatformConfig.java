package com.temis.app.config;

import com.temis.app.model.MessageSource;
import com.temis.app.service.MessagePlatformService;
import com.temis.app.service.impl.TwilioMessageService;
import com.temis.app.service.impl.WhatsappBusinessMessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuración para los servicios de plataformas de mensajería.
 */
@Configuration
public class MessagingPlatformConfig {

    @Bean
    public Map<MessageSource, MessagePlatformService> platformServices(
            TwilioMessageService twilioService,
            WhatsappBusinessMessageService businessService) {
        return Map.of(
            MessageSource.TWILIO, twilioService,
            MessageSource.META, businessService
        );
    }
}