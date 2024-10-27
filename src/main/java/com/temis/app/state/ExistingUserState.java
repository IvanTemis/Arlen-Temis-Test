package com.temis.app.state;

import com.temis.app.model.MessageHolderObject;
import com.temis.app.model.MessageResponseObject;
import com.temis.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;

@Component
public class ExistingUserState extends StateTemplate{
    @Autowired
    private UserRepository userRepository;

    public ExistingUserState() {
        super(new ArrayList<>(){

        });
    }

    @Override
    protected boolean ShouldTransition(MessageHolderObject message) {

        var users = userRepository.findByPhoneNumber(message.getPhoneNumber());

        if(!users.isEmpty()){
            message.setUserEntity(users.get(0));

            return true;
        }

        return false;
    }

    @Override
    protected void Execute(MessageHolderObject message, MessageResponseObject.MessageResponseObjectBuilder responseBuilder) {
        assert message.getUserEntity() != null;

        String name = message.getUserEntity().getNickName();

        if(message.getUserEntity().getFirstName() != null){
            name = message.getUserEntity().getFirstName();
        }

        responseBuilder.body(MessageFormat.format("¡Hola {0}! ¿En qué puedo ayudarte hoy?", name));
    }
}
