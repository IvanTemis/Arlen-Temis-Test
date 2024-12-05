package com.temis.app.state.with_user;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.state.StateTemplate;
import org.springframework.expression.AccessException;

import java.io.IOException;
import java.util.List;

public abstract class IntransitableWithUserStateTemplate extends StateWithUserTemplate {

    public IntransitableWithUserStateTemplate() {
        super(List.of());
    }


    protected abstract void Intransitable(MessageContextEntity message, UserEntity user) throws Exception;

    @Override
    protected boolean ShouldTransitionWithUser(MessageContextEntity message, UserEntity user) throws Exception {
        this.Intransitable(message, user);
        return false;
    }

    @Override
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) throws Exception {
        throw new AccessException("Intransitable State of type " + this.getClass().getName() + " entered the execute stage.");
    }
}
