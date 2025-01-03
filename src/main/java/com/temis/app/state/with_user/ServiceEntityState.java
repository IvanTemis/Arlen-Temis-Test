package com.temis.app.state.with_user;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.entity.ServiceEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.model.ServiceStage;
import com.temis.app.model.ServiceState;
import com.temis.app.repository.MessageContextRepository;
import com.temis.app.service.ServiceEntityService;
import com.temis.app.state.with_service.ClientVirtualAssistantState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

@Component
public class ServiceEntityState extends  StateWithUserTemplate{

    @Autowired
    private ServiceEntityService serviceEntityService;

    @Autowired
    private MessageContextRepository messageContextRepository;

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
            message.setServiceEntity(existingService);
        }
        else {
            ServiceEntity newService = ServiceEntity.hiddenBuilder()
                    .description("Nuevo servicio para el usuario: " + user.getSuitableName())
                    .phoneNumber(phoneNumber)
                    .isActive(true)
                    .creationDate(Timestamp.from(Instant.now()))
                    .priority(1)
                    .user(user)
                    .requirementEntities(new ArrayList<>())
                    .serviceState(ServiceState.PENDING)
                    .serviceStage(ServiceStage.SOCIETY_IDENTIFICATION)
                    .build();

            message.setServiceEntity(serviceEntityService.saveService(newService));
            log.info("Se creó un nuevo servicio para el usuario con número: {}", phoneNumber);
        }

        messageContextRepository.save(message);

        return true;
    }

    @Override
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user)  {
        responseBuilder.addContent("The Cake is a Lie");
    }

    @Override
    public MessageResponseEntity Evaluate(MessageContextEntity message) throws Exception {
        var result = super.Evaluate(message);
        var service = message.getServiceEntity();
        if(service != null) {
            //service.setLastInteractionDate(new Date());
            message.setServiceEntity(serviceEntityService.saveService(service));
        }
        return result;
    }
}
