package com.temis.app.state;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.repository.MessageContextRepository;
import com.temis.app.repository.UserRepository;
import com.temis.app.state.with_user.BeginDocumentCreationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;

@Component
public class ExistingUserState extends StateTemplate{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageContextRepository messageContextRepository;

    @Autowired
    public ExistingUserState(BeginDocumentCreationState beginDocumentCreationState) {
        super(new ArrayList<>(){{
            add(beginDocumentCreationState);
        }});
    }

    @Override
    protected boolean ShouldTransition(MessageContextEntity message) {

        var users = userRepository.findByPhoneNumber(message.getPhoneNumber());

        if(!users.isEmpty()){
            message.setUserEntity(users.get(0));
            messageContextRepository.save(message);
            return true;
        }

        return false;
    }

    @Override
    protected void Execute(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder) {
        assert message.getUserEntity() != null;

        String name = message.getUserEntity().getNickName();

        if(message.getUserEntity().getFirstName() != null){
            name = message.getUserEntity().getFirstName();
        }

        responseBuilder.body(MessageFormat.format("¡Hola {0}! ¿En qué puedo ayudarte hoy?", name));
        responseBuilder.quickActions(new ArrayList<>(){{
            add(BeginDocumentCreationState.compraventa);
        }});
    }
}
