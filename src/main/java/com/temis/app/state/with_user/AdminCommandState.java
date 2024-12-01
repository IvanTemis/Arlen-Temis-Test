package com.temis.app.state.with_user;

import com.temis.app.entity.*;
import com.temis.app.model.RequirementType;
import com.temis.app.repository.RequirementRepository;
import com.temis.app.repository.ServiceRepository;
import com.temis.app.repository.VertexAiContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AdminCommandState extends  StateWithUserTemplate{

    @Autowired
    VertexAiContentRepository vertexAiContextRepository;

    @Autowired
    public AdminCommandState() {
        super(new ArrayList<>());
    }

    @Override
    protected boolean ShouldTransitionWithUser(MessageContextEntity message, UserEntity user) {
        return message.getBody().startsWith("!");
    }

    @Override
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) throws URISyntaxException {

        var split = message.getBody().toLowerCase().split(" ");

        switch (split[0]){
            case "!lobotomía":
            case "!lobotomia":
            case "!clearhistory":
            case "!clearchat":
            case "!clearcontext":
            {
                var history = vertexAiContextRepository.findByUserEntityOrderByCreatedDateAsc(user);

                vertexAiContextRepository.deleteAll(history);

                responseBuilder.body("Contexto historico del agente limpiado.");
            }
            break;
            default:
                responseBuilder.body("Disculpa, no reconozco ese comando.");
                break;
        }

    }
}
