package com.temis.app.model;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessageParts {
    @Getter
    private String text;
    @Getter
    private String json;
    private Map<String, String> metadata; // Para futuros datos adicionales

    public MessageParts(String text, String json) {
        this.text = text;
        this.json = json;
        this.metadata = new HashMap<>();
    }

    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }

    public String getMetadata(String key) {
        return metadata.get(key);
    }

    public Map<String, String> getAllMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
}
