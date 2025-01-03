package com.temis.app.config;

import com.temis.app.config.properties.WhatsappApiConfigProperties;
import com.whatsapp.api.impl.WhatsappBusinessCloudApi;
import com.whatsapp.api.impl.WhatsappBusinessManagementApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WhatsappApiConfig {

    private final WhatsappApiConfigProperties whatsappApiConfigProperties;

    @Autowired
    public WhatsappApiConfig(WhatsappApiConfigProperties whatsappApiConfigProperties) {
        this.whatsappApiConfigProperties = whatsappApiConfigProperties;
    }

    @Bean
    public WhatsappBusinessCloudApi whatsappBusinessCloudApi(){
        return new WhatsappBusinessCloudApi(whatsappApiConfigProperties.token(), whatsappApiConfigProperties.apiVersion());
    }

    @Bean
    public WhatsappBusinessManagementApi whatsappBusinessManagementApi(){
        return  new WhatsappBusinessManagementApi(whatsappApiConfigProperties.token(), whatsappApiConfigProperties.apiVersion());
    }
}
