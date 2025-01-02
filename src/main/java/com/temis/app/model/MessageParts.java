package com.temis.app.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessageParts {
    private String text;
    private String json;
    private Map<String, String> metadata; // Para futuros datos adicionales

    public MessageParts(String text, String json) {
        this.text = text;
        this.json = json;
        this.metadata = new HashMap<>();
    }

    public String getText() {
        return text;
    }

    public String getJson() {
        return json;
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
