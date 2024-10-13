package com.temis.app.config;

import com.temis.app.client.VertexAIClient;

import java.io.IOException;

import com.temis.app.config.properties.VertexAiConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertexAiConfig {

    private final VertexAiConfigProperties vertexAiConfigProperties;

    @Autowired
    public VertexAiConfig(VertexAiConfigProperties vertexAiConfigProperties) {
        this.vertexAiConfigProperties = vertexAiConfigProperties;
    }

    @Bean
    public VertexAIClient vertexAIClient() throws IOException {
        return new VertexAIClient(
                vertexAiConfigProperties.projectId(),
                vertexAiConfigProperties.location(),
                vertexAiConfigProperties.modelName()
        );
    }
}