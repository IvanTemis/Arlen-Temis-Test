package com.temis.app.state.with_user;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.entity.ServiceEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.service.ServiceEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

@Component
public class ServiceEntityState extends  StateWithUserTemplate{

    @Autowired
    private ServiceEntityService serviceEntityService;

    @Autowired
    public ServiceEntityState(ClientVirtualAssistantState clientVirtualAssistantState) {
        super(new ArrayList<>(){{
            add(clientVirtualAssistantState);
        }});
    }

    @Override
    protected boolean ShouldTransitionWithUser(MessageContextEntity message, UserEntity user) {

        var phoneNumber = user.getPhoneNumber();

        ServiceEntity existingService = serviceEntityService.findActiveServiceByPhoneNumber(phoneNumber);

        if (existingService != null) {
            log.info("El usuario con número {} ya tiene un servicio activo: {}", phoneNumber, existingService.getId());
        }
        else {
            ServiceEntity newService = ServiceEntity.hiddenBuilder()
                    .description("Nuevo servicio para el usuario: " + phoneNumber)
                    .phoneNumber(phoneNumber)
                    .isActive(true)
                    .creationDate(Timestamp.from(Instant.now()))
                    .priority(1)
                    .user(user)
                    .serviceState(com.temis.app.model.ServiceState.PENDING)
                    .build();

            serviceEntityService.saveService(newService);
            log.info("Se creó un nuevo servicio para el usuario con número: {}", phoneNumber);
        }

        return true;
    }

    @Override
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user)  {
        responseBuilder.body("The Cake is a Lie");
    }
}
