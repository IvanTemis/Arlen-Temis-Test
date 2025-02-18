package com.temis.app.placeholder.provider;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.exception.PlaceholderNotFoundException;
import com.temis.app.placeholder.PlaceholderProvider;
import org.springframework.stereotype.Component;

@Component
public class ContextPlaceholderProvider implements PlaceholderProvider {
    @Override
    public String GetPrefix() {
        return "context";
    }

    @Override
    public String Evaluate(MessageContextEntity messageContext, String arguments) throws Exception {

        var split = arguments.split("\\.");

        Object parent = messageContext;
        Object result = null;
        for (String str : split) {
            if(str.endsWith(")")){
                var method = parent.getClass().getMethod(str.substring(0, str.indexOf('(')));
                result = method.invoke(parent);
            }
            else{
                var field = parent.getClass().getField(str);
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
