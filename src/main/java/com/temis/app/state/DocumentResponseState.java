package com.temis.app.state;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class DocumentResponseState extends StateTemplate{

    @Autowired
    public DocumentResponseState() {
        super(new ArrayList<>());
    }

    @Override
    protected boolean ShouldTransition(MessageContextEntity message) {
        return message.getDocumentEntity() != null;
    }

    @Override
    protected void Execute(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder) {
        assert message.getDocumentEntity() != null;

        var documentType = message.getDocumentEntity().getDocumentType();

        if(documentType == null){
            responseBuilder.body("null");
            return;
        }

        responseBuilder.body(documentType.getName());
    }
}
