package com.temis.app.state;

import com.temis.app.model.MessageHolderObject;
import com.temis.app.model.MessageResponseObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FirstContactState extends StateTemplate{

    public FirstContactState() {
        super(new ArrayList<>());
    }

    //Como este es el estado de entrada, no hay necesidad de evaluar nada
    @Override
    public boolean ShouldTransition(MessageHolderObject message) {
        return true;
    }

    @Override
    protected void Execute(MessageHolderObject message, MessageResponseObject.MessageResponseObjectBuilder responseBuilder) {

        responseBuilder.body("Hola mamahuevo");

    }

}
