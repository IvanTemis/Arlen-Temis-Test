package com.temis.app.config;

import com.temis.app.client.ChatAIClient;
import com.temis.app.client.CloudStorageClient;
import com.temis.app.client.VertexAIClient;

import java.io.IOException;

import com.temis.app.config.properties.CloudConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudConfig {

    private final CloudConfigProperties cloudConfigProperties;

    @Autowired
    public CloudConfig(CloudConfigProperties cloudConfigProperties) {
        this.cloudConfigProperties = cloudConfigProperties;
    }

    @Bean
    public VertexAIClient vertexAIClient() throws IOException {
        return new VertexAIClient(
                cloudConfigProperties.getProjectId(),
                cloudConfigProperties.getLocation(),
                cloudConfigProperties.getVertexai().getModelName()
        );
    }

    @Bean
    public ChatAIClient chatAIClient() throws IOException {
        return new ChatAIClient(
                cloudConfigProperties.getProjectId(),
                cloudConfigProperties.getLocation(),
                cloudConfigProperties.getVertexai().getModelName()
        );
    }

    @Bean
    public CloudStorageClient cloudStorageClient() {
        return new CloudStorageClient(
                cloudConfigProperties.getProjectId(),
                cloudConfigProperties.getStorage().getBucketName()
        );
    }
}