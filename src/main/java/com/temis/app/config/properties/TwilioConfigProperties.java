package com.temis.app.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("twilio")
public record TwilioConfigProperties(String accountSid, String authToken, String phoneNumber) {
}
