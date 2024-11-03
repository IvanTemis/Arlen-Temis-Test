package com.temis.app.state;

import com.temis.app.entity.UserEntity;
import com.temis.app.model.MessageContext;
import com.temis.app.model.MessageResponseObject;
import com.temis.app.repository.UserRepository;
import com.temis.app.state.intransitable.RecordHistoryIntransitableState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;

@Component
public class FirstContactState extends StateTemplate{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public FirstContactState(ExistingUserState registeredUserState, RecordHistoryIntransitableState recordHistoryIntransitableState) {
        super(new ArrayList<>(){{
            add(recordHistoryIntransitableState);
            add(registeredUserState);
        }});
    }

    //Como este es el estado de entrada, no se ejecuta este código
    @Override
    protected boolean ShouldTransition(MessageContext message) {
        return true;
    }

    @Override
    protected void Execute(MessageContext message, MessageResponseObject.MessageResponseObjectBuilder responseBuilder) {
        var newUser = new UserEntity();

        newUser.setPhoneNumber(message.getPhoneNumber());
        newUser.setNickName(message.getNickName());
        newUser.setCreationDate(new Timestamp(System.currentTimeMillis()));
        newUser.setIsActive(true);

        userRepository.save(newUser);

        responseBuilder.body("Hola mamahuevo");
    }

}
