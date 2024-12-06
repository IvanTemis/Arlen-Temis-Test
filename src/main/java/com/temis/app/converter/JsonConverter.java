package com.temis.app.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Converter
@Slf4j
public class JsonConverter implements AttributeConverter<Map<String, Object>, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<Map<String, Object>> mapTypeRef = new TypeReference<Map<String, Object>>() {
    };

    @Override
    public String convertToDatabaseColumn(Map<String, Object> data) {

        if (null == data) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            log.error("Couldn't convert data-map to JSON String.", ex);
            return null;
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {

        if (null == dbData || dbData.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(dbData, mapTypeRef);
        } catch (IOException ex) {
            log.error("Couldn't convert JSON String to data-map.", ex);
            return null;
        }
    }
}
