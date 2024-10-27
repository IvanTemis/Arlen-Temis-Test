package com.temis.app.state.with_user;

import com.temis.app.entity.UserEntity;
import com.temis.app.model.MessageHolderObject;
import com.temis.app.model.MessageResponseObject;
import com.temis.app.state.StateTemplate;

import java.util.List;

public abstract class StateWithUserTemplate extends StateTemplate {
    public StateWithUserTemplate(List<StateTemplate> otherStates) {
        super(otherStates);
    }

    @Override
    protected boolean ShouldTransition(MessageHolderObject message) {
        assert message.getUserEntity() != null;

        return ShouldTransitionWithUser(message, message.getUserEntity());
    }

    protected abstract boolean ShouldTransitionWithUser(MessageHolderObject message, UserEntity user);

    @Override
    protected void Execute(MessageHolderObject message, MessageResponseObject.MessageResponseObjectBuilder responseBuilder) {
        assert message.getUserEntity() != null;

        ExecuteWithUser(message, responseBuilder, message.getUserEntity());
    }

    protected abstract void ExecuteWithUser(MessageHolderObject message, MessageResponseObject.MessageResponseObjectBuilder responseBuilder, UserEntity user);
}
