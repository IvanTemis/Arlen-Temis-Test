package com.temis.app.config.properties;

import com.whatsapp.api.configuration.ApiVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("whatsappapi")
public record WhatsappApiConfigProperties(String token, ApiVersion apiVersion, String businessAccountId, String phoneNumberId) {
}
