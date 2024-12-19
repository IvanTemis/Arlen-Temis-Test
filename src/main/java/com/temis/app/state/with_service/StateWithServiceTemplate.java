package com.temis.app.state.with_service;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.entity.ServiceEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.state.StateTemplate;

import java.util.List;

public abstract class StateWithServiceTemplate extends StateTemplate {
    public StateWithServiceTemplate(List<StateTemplate> otherStates) {
        super(otherStates);
    }

    @Override
    protected boolean ShouldTransition(MessageContextEntity message) throws Exception {
        assert message.getUserEntity() != null;
        assert message.getServiceEntity() != null;

        return ShouldTransitionWithService(message, message.getUserEntity(), message.getServiceEntity());
    }

    protected abstract boolean ShouldTransitionWithService(MessageContextEntity message, UserEntity user, ServiceEntity service) throws Exception;

    @Override
    protected void Execute(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder) throws Exception {
        assert message.getUserEntity() != null;
        assert message.getServiceEntity() != null;

        ExecuteWithService(message, responseBuilder, message.getUserEntity(), message.getServiceEntity());
    }

    protected abstract void ExecuteWithService(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user, ServiceEntity service) throws Exception;
}
