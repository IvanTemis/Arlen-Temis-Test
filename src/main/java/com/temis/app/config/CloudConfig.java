package com.temis.app.config;

import com.temis.app.agent.DocumentClassifierAgent;
import com.temis.app.agent.EmailContentCreatorAgent;
import com.temis.app.client.*;

import java.io.IOException;

import com.temis.app.config.properties.CloudConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class CloudConfig {

    private final CloudConfigProperties cloudConfigProperties;

    @Autowired
    public CloudConfig(CloudConfigProperties cloudConfigProperties) {
        this.cloudConfigProperties = cloudConfigProperties;
    }

    @Bean
    public CloudTaskClient cloudTaskClient() {
        return new CloudTaskClient(
                cloudConfigProperties.getProjectId(),
                cloudConfigProperties.getLocation(),
                cloudConfigProperties.getServiceUrl(),
                cloudConfigProperties.getServiceAccountEmail()
        );
    }

    @Bean
    public CloudStorageClient cloudStorageClient() {
        return new CloudStorageClient(
                cloudConfigProperties.getProjectId(),
                cloudConfigProperties.getStorage().getBucketName()
        );
    }

    @Bean
    public GoogleCalendarClient googleCalendarClient() throws IOException {
        return new GoogleCalendarClient(
                cloudConfigProperties.getCalendar().getApplicationName()
        );
    }

    @Bean
    public VertexAIClient vertexAIClient() {
        return new VertexAIClient(
                cloudConfigProperties.getProjectId(),
                cloudConfigProperties.getLocation(),
                cloudConfigProperties.getVertexai().getModelName()
        );
    }

}