package com.temis.app.placeholder;

import com.temis.app.entity.MessageContextEntity;

import java.util.Map;

public interface PlaceholderProvider {
    String GetPrefix();

    String Evaluate(Map<String, Object> context, String arguments) throws Exception;
}
