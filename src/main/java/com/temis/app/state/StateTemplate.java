package com.temis.app.state;

import com.temis.app.model.MessageContext;
import com.temis.app.model.MessageResponseObject;

import java.util.List;


public abstract class StateTemplate {

    private final List<StateTemplate> _otherStates;

    public StateTemplate(List<StateTemplate> otherStates) {
        _otherStates = otherStates;
    }

    public MessageResponseObject Evaluate(MessageContext message){
        for (var state : _otherStates){
            if(state.ShouldTransition(message)){
                return state.Evaluate(message);
            }
        }

        var responseBuilder = MessageResponseObject.builder();

        responseBuilder.phoneNumber(message.getPhoneNumber());

        Execute(message, responseBuilder);

        return responseBuilder.build();
    }

    //La información del MessageHolderObject puede ser modificada durante esta etapa para futuro uso en otros estados
    protected abstract boolean ShouldTransition(MessageContext message);

    protected abstract void Execute(MessageContext message, MessageResponseObject.MessageResponseObjectBuilder responseBuilder);
}
