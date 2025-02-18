package com.temis.app.placeholder.provider;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.exception.PlaceholderNotFoundException;
import com.temis.app.placeholder.PlaceholderProvider;
import org.springframework.stereotype.Component;

@Component
public class GeneralPlaceholderProvider implements PlaceholderProvider {
    @Override
    public String GetPrefix() {
        return "general";
    }

    @Override
    public String Evaluate(MessageContextEntity messageContext, String arguments) throws PlaceholderNotFoundException {
        switch (arguments){
            case "now", "today" -> {
                return java.time.LocalDateTime.now().toString();
            }
            case "yesterday" -> {
                return java.time.LocalDateTime.now().minusDays(1).toString();
            }
            case "tomorrow" -> {
                return java.time.LocalDateTime.now().plusDays(1).toString();
            }
            default -> throw new PlaceholderNotFoundException("Placeholder in provider '" + GetPrefix() + "' with arguments '" + arguments + "' not found.");
        }
    }
}
