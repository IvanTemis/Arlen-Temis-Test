package com.temis.app.config.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Slf4j
@Getter
@Setter
@ConfigurationProperties("cloud")
public class CloudConfigProperties {
    
    private String projectId;
    private String location;
    private String serviceUrl;
    private String serviceAccountEmail;

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

    @PostConstruct
    void LogFields(){
        if (log.isInfoEnabled()) {
            log.info("SERVICE URL IS '{}'", this.serviceUrl);
        }
    }
}