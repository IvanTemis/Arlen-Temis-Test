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
        return message.getBodies().stream().anyMatch(b -> b.startsWith("!"));
    }

    @Override
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) throws IOException, MessagingException {
        StringBuilder stringBuilder = new StringBuilder();
        for (var content : message.getMessageContents()) {
            var split = content.getBody().toLowerCase().split(" ");

            var command = split[0];

            switch (command) {
                case "!lobotomía":
                case "!lobotomia":
                case "!clearhistory":
                case "!clearchat":
                case "!clearcontext": {
                    var history = vertexAiContextRepository.findByUserEntityOrderByCreatedDateAsc(user);

                    vertexAiContextRepository.deleteAll(history);

                    user.setLastInteractionDate(null);
                    userRepository.save(user);

                    serviceEntityService.deactivateServicesForUser(user);

                    stringBuilder.append("Contexto historico del agente limpiado.");
                }
                break;
                case "!fetchprompt":
                case "!updateprompt":
                case "!refreshprompt": {
                    for (ChatAIClient agent : agentManager.getAllAgents()) {
                        agent.UpdatePrompt();
                    }
                    documentClassifierClient.UpdatePrompt();
                    stringBuilder.append("Prompts actualizados exitosamente.");
                }
                break;
                case "!emailtest": {

                    var email = user.getEmail();

                    if (email == null) {
                        stringBuilder.append("Disculpa, no puedo enviarte este correo porque no tienes un correo asignado.");
                        break;
                    }

                    var emailBody = content.getBody().substring(command.length() + 1);
                    var document = content.getDocumentEntity();
                    if (document != null) {
                        var bytes = cloudStorageClient.ReadFileBytes(document.getPath());

                        emailService.SendHtmlEmailWithAttachments(email, "TEST", emailBody, Pair.of(document.getName(), new ByteArrayResource(bytes)));
                    } else {
                        emailService.SendSimpleEmail(email, "TEST", emailBody);
                    }

                    stringBuilder.append("Email de prueba enviado a tu correo.");
                }
                break;
                case "!resend": {
                    log.info("Resending message with ID {}", split[1]);
                    long id = Long.parseLong(split[1], 10);
                    var resend = messageResponseRepository.findById(id);

                    if (resend.isEmpty()) {
                        stringBuilder.append("Respuesta con ID ").append(id).append(" no encontrada.");
                        break;
                    }

                    var resp = resend.get();

                    responseBuilder
                            .body(resp.getBody())
                            .mediaURL(resp.getMediaURL());
                    return; //TODO: Modificar cuando se puedan enviar múltiples respuestas en un solo mensaje.
                }
                //break;
                default:
                    stringBuilder.append("Disculpa, no reconozco ese comando.");
                    break;
            }
            stringBuilder.append("?LL?");
        }

        responseBuilder.body(stringBuilder.toString());
    }
}
