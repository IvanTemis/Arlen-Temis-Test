package com.temis.app.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties("cloud")
public class CloudConfigProperties {
    
    private String projectId;
    private String location;

    @NestedConfigurationProperty
    private VertexAiConfigProperties vertexai;

    @NestedConfigurationProperty
    private StorageConfigProperties storage;

    @NestedConfigurationProperty
    private CalendarConfigProperties calendar;

    @Getter
    @Setter
    public static class VertexAiConfigProperties {
        private String modelName;
    }

    @Getter
    @Setter
    public static class StorageConfigProperties {
        private String bucketName;
    }

    @Getter
    @Setter
    public static class CalendarConfigProperties {
        private String applicationName;
    }
}