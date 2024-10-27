package com.temis.app.state.intransitable;

import com.temis.app.model.MessageHolderObject;
import com.temis.app.model.MessageResponseObject;
import com.temis.app.state.StateTemplate;

import java.util.ArrayList;
import java.util.List;

public abstract class IntransitableStateTemplate extends StateTemplate {
    public IntransitableStateTemplate() {
        super(new ArrayList<>()); //Esto nunca va a pasar de ShouldTransition, así que nunca entrará a los sub-estados
    }

    @Override
    protected boolean ShouldTransition(MessageHolderObject message) {
        Intransitable(message);
        return false; //Siempre retornamos false, nuestro objetivo con esta clase es ejecutar código en esta etapa sin entrar en Execute
    }

    protected abstract void Intransitable(MessageHolderObject message);

    @Override
    protected void Execute(MessageHolderObject message, MessageResponseObject.MessageResponseObjectBuilder responseBuilder) {
        //Este código nunca se debería llegar a ejecutar.
        responseBuilder.body("The cake is a lie.");
    }
}
