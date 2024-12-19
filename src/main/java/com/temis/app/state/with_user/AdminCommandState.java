package com.temis.app.state.with_user;

import com.temis.app.client.ChatAIClient;
import com.temis.app.client.CloudStorageClient;
import com.temis.app.client.DocumentClassifierClient;
import com.temis.app.entity.*;
import com.temis.app.manager.AgentManager;
import com.temis.app.repository.MessageResponseRepository;
import com.temis.app.repository.UserRepository;
import com.temis.app.repository.VertexAiContentRepository;
import com.temis.app.service.EmailService;
import com.temis.app.service.ServiceEntityService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class AdminCommandState extends  StateWithUserTemplate{

    @Autowired
    VertexAiContentRepository vertexAiContextRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MessageResponseRepository messageResponseRepository;

    @Autowired
    AgentManager agentManager;
    @Autowired
    private DocumentClassifierClient documentClassifierClient;

    @Autowired
    EmailService emailService;
    @Autowired
    ServiceEntityService serviceEntityService;

    @Autowired
    private CloudStorageClient cloudStorageClient;

    @Autowired
    public AdminCommandState() {
        super(new ArrayList<>());
    }

    @Override
    protected boolean ShouldTransitionWithUser(MessageContextEntity message, UserEntity user) {
        return message.getBody().startsWith("!");
    }

    @Override
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) throws IOException, MessagingException {

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

                user.setLastInteractionDate(null);
                userRepository.save(user);

                serviceEntityService.deactivateServicesForUser(user);

                responseBuilder.body("Contexto historico del agente limpiado.");
            }
            break;
            case "!fetchprompt":
            case "!updateprompt":
            case "!refreshprompt":
            {
                ChatAIClient chatAIClient = agentManager.getAgent("agent123");
                chatAIClient.UpdatePrompt("agent123");
                documentClassifierClient.UpdatePrompt();
                responseBuilder.body("Prompt actualizado exitosamente.");
            }
            break;
            case "!emailtest":{

                var email = user.getEmail();

                if(email == null){
                    responseBuilder.body("Disculpa, no puedo enviarte este correo porque no tienes un correo asignado.");
                    break;
                }

                var body = message.getBody().substring(command.length() + 1);
                var document = message.getDocumentEntity();
                if(document != null){
                    var bytes = cloudStorageClient.ReadFileBytes(document.getPath());

                    emailService.SendHtmlEmailWithAttachments(email, "TEST", body, Pair.of(document.getName(), new ByteArrayResource(bytes)));
                }
                else{
                    emailService.SendSimpleEmail(email, "TEST", body);
                }

                responseBuilder.body("Email de prueba enviado a tu correo.");
            }
            break;
            case "!resend":
            {
                log.info("Resending message with ID {}", split[1]);
                long id = Long.parseLong(split[1], 10);
                var resend = messageResponseRepository.findById(id);

                if(resend.isEmpty()){
                    responseBuilder.body("Respuesta con ID " + id + " no encontrada.");
                    break;
                }

                var resp = resend.get();

                responseBuilder
                        .body(resp.getBody())
                        .mediaURL(resp.getMediaURL());
            }
            break;
            default:
                responseBuilder.body("Disculpa, no reconozco ese comando.");
                break;
        }

    }
}
