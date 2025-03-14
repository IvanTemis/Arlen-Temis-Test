package com.temis.app.placeholder.provider;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.exception.PlaceholderNotFoundException;
import com.temis.app.placeholder.PlaceholderProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ContextPlaceholderProvider implements PlaceholderProvider {
    @Override
    public String GetPrefix() {
        return "context";
    }

    @Override
    public String Evaluate(Map<String, Object> context, String arguments) throws Exception {
        var sepIndex = arguments.indexOf('.');
        var contextName = arguments.substring(0, sepIndex);

        if(!context.containsKey(contextName)) throw new PlaceholderNotFoundException("Placeholder in provider '" + GetPrefix() + "' with arguments '" + arguments + "' not found.");

        var split = arguments.substring(sepIndex + 1).split("\\.");

        Object parent = context.get(contextName);
        Object result = null;
        for (String str : split) {
            if(str.endsWith(")")){
                var method = parent.getClass().getDeclaredMethod(str.substring(0, str.indexOf('(')));
                method.setAccessible(true);
                result = method.invoke(parent);
            }
            else{
                var field = parent.getClass().getDeclaredField(str);
                field.setAccessible(true);
                result = field.get(parent);
            }

            parent = result;
        }

        //Si result = null & parent = null entonces field.get() retornó null.
        if(result == null && parent != null) throw new PlaceholderNotFoundException("Placeholder in provider '" + GetPrefix() + "' with arguments '" + arguments + "' not found.");
        if(result == null) return "null";
        return result.toString();
    }
}
