package com.temis.app.state.with_user;

import com.temis.app.entity.*;
import com.temis.app.model.RequirementType;
import com.temis.app.repository.RequirementRepository;
import com.temis.app.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class BeginDocumentCreationState extends  StateWithUserTemplate{
    public static String compraventa = "Crear Compra-Venta";

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private RequirementRepository requirementRepository;

    @Autowired
    public BeginDocumentCreationState() {
        super(new ArrayList<>(){{

        }});
    }

    @Override
    protected boolean ShouldTransitionWithUser(MessageContextEntity message, UserEntity user) {
        return message.getBody().equals(compraventa);
    }

    @Override
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) throws URISyntaxException {

        assert user.getEmployee() != null;

        var service = ServiceEntity.builder(user.getEmployee())
                .description("Mamahuevo")
                .isActive(true)
                .build();

        serviceRepository.save(service);

        List<RequirementEntity> requirements = List.of(
                RequirementEntity.builder(service).requirementType(RequirementType.DOCUMENT).build()
        );

        requirementRepository.saveAll(requirements);

        responseBuilder.body("*No hace nada*");

        responseBuilder.mediaURL(new URI("https://images7.memedroid.com/images/UPLOADED213/63f303757e38e.jpeg"));

    }
}
