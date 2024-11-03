package com.temis.app.state.intransitable;

import com.temis.app.model.MessageContext;
import org.springframework.stereotype.Component;

@Component
public class RecordHistoryIntransitableState extends IntransitableStateTemplate{
    @Override
    protected void Intransitable(MessageContext message) {
        //TODO: Guardar mensaje en el historial de mensajes
    }
}
