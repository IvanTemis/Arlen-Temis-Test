package com.temis.app.state.with_user;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.state.StateTemplate;

import java.util.List;

public abstract class StateWithUserTemplate extends StateTemplate {
    public StateWithUserTemplate(List<StateTemplate> otherStates) {
        super(otherStates);
    }

    @Override
    protected boolean ShouldTransition(MessageContextEntity message) throws Exception {
        assert message.getUserEntity() != null;

        return ShouldTransitionWithUser(message, message.getUserEntity());
    }

    protected abstract boolean ShouldTransitionWithUser(MessageContextEntity message, UserEntity user) throws Exception;

    @Override
    protected void Execute(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder) throws Exception {
        assert message.getUserEntity() != null;

        ExecuteWithUser(message, responseBuilder, message.getUserEntity());
    }

    protected abstract void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) throws Exception;
}
