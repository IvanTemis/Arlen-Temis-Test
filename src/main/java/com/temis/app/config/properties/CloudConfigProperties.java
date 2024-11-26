package com.temis.app.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@ConfigurationProperties("cloud")
public class CloudConfigProperties {
    String projectId;
    String location;

    @NestedConfigurationProperty
    VertexAiConfigProperties vertexai;

    @NestedConfigurationProperty
    StorageConfigProperties storage;


    @Getter
    public static class VertexAiConfigProperties{
        String modelName;
    }

    @Getter
    public static class StorageConfigProperties{
        String bucketName;
    }
}
