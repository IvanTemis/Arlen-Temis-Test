package com.temis.app.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter @Setter
@ConfigurationProperties("cloud")
public class CloudConfigProperties {
    String projectId;
    String location;

    @NestedConfigurationProperty
    VertexAiConfigProperties vertexai;

    @NestedConfigurationProperty
    StorageConfigProperties storage;


    @Getter @Setter
    public static class VertexAiConfigProperties{
        String modelName;
    }

    @Getter @Setter
    public static class StorageConfigProperties{
        String bucketName;
    }
}
