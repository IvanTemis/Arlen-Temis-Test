package com.temis.app.placeholder;

import com.temis.app.entity.MessageContextEntity;

public interface PlaceholderProvider {
    String GetPrefix();

    String Evaluate(MessageContextEntity messageContext, String arguments) throws Exception;
}
