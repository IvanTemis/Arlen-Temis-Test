package com.temis.app.state.with_user;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

@Component
public class BeginDocumentCreationState extends  StateWithUserTemplate{
    public static String compraventa = "Crear Compra-Venta";

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
    protected void ExecuteWithUser(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder, UserEntity user) {
        responseBuilder.body("*No hace nada*");
        try {
            responseBuilder.mediaURL(new URI("https://images7.memedroid.com/images/UPLOADED213/63f303757e38e.jpeg"));
        } catch (URISyntaxException ignored) {
        }
    }
}
