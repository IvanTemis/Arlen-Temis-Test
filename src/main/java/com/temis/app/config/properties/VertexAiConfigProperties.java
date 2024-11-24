package com.temis.app.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("vertexai")
public record VertexAiConfigProperties(String projectId, String location, String modelName) {
}
