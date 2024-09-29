package com.temis.app.config;

import com.temis.app.client.VertexAIClient;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertexAiConfig {

    @Bean
    public VertexAIClient vertexAIClient() throws IOException {
        return new VertexAIClient("temis-430707", "us-central1", "gemini-1.5-flash-002");
    }
}