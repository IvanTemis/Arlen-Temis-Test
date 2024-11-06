package com.temis.app.state;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.repository.MessageContextRepository;
import com.temis.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;

@Component
public class FirstContactState extends StateTemplate{

    @Autowired
    private UserRepository userRepository;
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
        var newUser = new UserEntity();

        newUser.setPhoneNumber(message.getPhoneNumber());
        newUser.setNickName(message.getNickName());
        newUser.setIsActive(true);

        userRepository.save(newUser);

        responseBuilder.body("Hola mamahuevo");
    }

}
