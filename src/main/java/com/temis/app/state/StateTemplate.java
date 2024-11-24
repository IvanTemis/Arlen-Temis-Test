package com.temis.app.state;

import com.google.api.gax.rpc.ResourceExhaustedException;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.repository.MessageResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class StateTemplate {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected MessageResponseRepository messageResponseRepository;

    private final List<StateTemplate> _otherStates;

    public StateTemplate(List<StateTemplate> otherStates) {
        _otherStates = otherStates;
    }

    protected void PreEvaluate(MessageContextEntity message) {
        log.debug("Pre-Evaluating state.");
    }

    public MessageResponseEntity Evaluate(MessageContextEntity message){
        PreEvaluate(message);
        log.info("Evaluating State for Message with Id: {}", message.getId());
        log.debug("Processing {} possible transitions.", _otherStates.size());
        for (var state : _otherStates){
            log.debug("Checking if should transition into {}.", state.getClass().getSimpleName());
            if(state.ShouldTransition(message)){
                return state.Evaluate(message);
            }
        }

        var responseBuilder = MessageResponseEntity.builder()
                .messageContextEntity(message)
                .phoneNumber(message.getPhoneNumber())
                .userEntity(message.getUserEntity());

        log.info("Executing State for Message with Id: {}", message.getId());

        Exception exception = null;
        String exceptionMessage = "The cake is a lie.";

        try {
            Execute(message, responseBuilder);
        }
        catch (ResourceExhaustedException e){
            exception = e;
            exceptionMessage = "Se ha superado la quota por minuto. Por favor espera un momento antes de continuar la conversación.";
        }
        catch (Exception e) {
            exception = e;
            exceptionMessage = "Parece que hubo un error durante tu solicitud. Por favor contacta al administrador.";
        }

        if(exception != null){
            //Creamos un builder vacío para asegurar el contenido del mensaje de error;
            responseBuilder = MessageResponseEntity.builder()
                    .messageContextEntity(message)
                    .phoneNumber(message.getPhoneNumber())
                    .userEntity(message.getUserEntity())
                    .body(exceptionMessage);
            log.error("An error occurred during state evaluation", exception);
        }

        return messageResponseRepository.save(responseBuilder.build());
    }

    //La información del MessageHolderObject puede ser modificada durante esta etapa para futuro uso en otros estados
    protected abstract boolean ShouldTransition(MessageContextEntity message);

    protected abstract void Execute(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder) throws Exception;
}
