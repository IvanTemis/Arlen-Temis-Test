package com.temis.app.state.with_user;

import com.temis.app.client.ChatAIClient;
import com.temis.app.entity.*;
import com.temis.app.model.RequirementType;
import com.temis.app.repository.RequirementRepository;
import com.temis.app.repository.ServiceRepository;
import com.temis.app.repository.VertexAiContentRepository;
import com.temis.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AdminCommandState extends  StateWithUserTemplate{

    @Autowired
    VertexAiContentRepository vertexAiContextRepository;

    @Autowired
    ChatAIClient chatAIClient;

    @Autowired
    EmailService emailService;

    @Autowired
    public AdminCommandState() {
        super(new ArrayList<>());
    }

    @Override
    protected boolean ShouldTransitionWithUser(MessageContextEntity message, UserEntity user) {
        return message.getBody().startsWith("!");
    }

    @Override
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) throws IOException {

        var split = message.getBody().toLowerCase().split(" ");

        var command = split[0];

        switch (command){
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
            case "!fetchprompt":
            case "!updateprompt":
            case "!refreshprompt":
            {
                chatAIClient.UpdatePrompt();
                responseBuilder.body("Prompt actualizado exitosamente.");
            }
            break;
            case "!emailtest":{

                var email = user.getEmail();

                if(email == null){
                    responseBuilder.body("Disculpa, no puedo enviarte este correo porque no tienes un correo asignado.");
                    break;
                }

                emailService.SendSimpleEmail(email, "TEST", message.getBody().substring(command.length() + 1));

                responseBuilder.body("Email de prueba enviado a tu correo.");
            }
            break;
            default:
                responseBuilder.body("Disculpa, no reconozco ese comando.");
                break;
        }

    }
}
