package com.temis.app.state.intransitable;

import com.temis.app.model.MessageHolderObject;
import org.springframework.stereotype.Component;

@Component
public class RecordHistoryIntransitableState extends IntransitableStateTemplate{
    @Override
    protected void Intransitable(MessageHolderObject message) {
        //TODO: Guardar mensaje en el historial de mensajes
    }
}
