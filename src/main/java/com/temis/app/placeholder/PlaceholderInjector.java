package com.temis.app.placeholder;

import com.temis.app.entity.MessageContextEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PlaceholderInjector {
    private static final String PATTERN = "(%%.*%%)";

    private final Map<String, PlaceholderProvider> providerMap;

    @Autowired
    public PlaceholderInjector(List<PlaceholderProvider> providers){
        providerMap = new HashMap<>();
        log.info("Found {} placeholder providers!", providers.size());
        for (PlaceholderProvider provider : providers) {
            providerMap.put(provider.GetPrefix(), provider);
        }
    }

    public String Inject(String prompt, MessageContextEntity messageContext){
        StringBuilder sb = new StringBuilder();
        var matcher = Pattern.compile(PATTERN).matcher(prompt);

        log.info("Processing placeholders...");
        while (matcher.find()){
            var placeholder = matcher.group(1);
            log.info("Processing placeholder '{}'...", placeholder);
            var separatorIndex = placeholder.indexOf(':');
            var prefix = placeholder.substring(2, separatorIndex);

            if(providerMap.containsKey(prefix)){
                var args =  placeholder.substring(separatorIndex + 1, placeholder.length() - 2);
                log.info("Found provider '{}' for placeholder '{}' with args {}!", prefix, placeholder, args);

                try{
                    var res = providerMap.get(prefix).Evaluate(messageContext,args);
                    log.info("Placeholder '{}' resulted in '{}'", placeholder, res);
                    placeholder = res;
                } catch (Exception e) {
                    log.error("Error while evaluating placeholder '" + placeholder + "'",e);
                    placeholder = "error:" + e.toString();
                }
            }
            else{
                log.warn("No provider could be found for prefix '{}' of placeholder '{}'", prefix, placeholder);
            }

            matcher.appendReplacement(sb, placeholder);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
