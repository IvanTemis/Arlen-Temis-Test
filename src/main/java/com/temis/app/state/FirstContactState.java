package com.temis.app.state;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.repository.MessageContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FirstContactState extends StateTemplate{

    @Autowired
    private MessageContextRepository messageContextRepository;

    @Autowired
    public FirstContactState(ExistingUserState registeredUserState) {
        super(new ArrayList<>(){{
            add(registeredUserState);
        }});
    }

    //Como este es el estado de entrada, no se ejecuta este código
    @Override
    protected boolean ShouldTransition(MessageContextEntity message) {
        return true;
    }

    @Override
    protected void PreEvaluate(MessageContextEntity message) {
        super.PreEvaluate(message);
        messageContextRepository.save(message);
    }

    @Override
    protected void Execute(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder) {

        responseBuilder.body("¡Hola! Soy el agente de IA de TEMIS.\nActualmente me encuentro en *Beta Cerrada* y parece que no has sido habilitado para usar este servicio.\n\n(Si consideras que esto es un error, por favor contacta a los administradores)");
    }

}
