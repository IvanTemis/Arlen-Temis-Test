package com.temis.app.state;

import com.temis.app.entity.MessageContextContentEntity;
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
        return message.getMessageContents().stream().anyMatch(c -> c.getDocumentEntity() != null);
    }

    @Override
    protected void Execute(MessageContextEntity message, MessageResponseEntity.MessageResponseEntityBuilder responseBuilder) {
        for (MessageContextContentEntity content : message.getMessageContents()) {
            var document = content.getDocumentEntity();

            if(document == null) continue;

            var documentType = document.getDocumentType();

            if(documentType == null){
                responseBuilder.addContent("null");
                continue;
            }

            responseBuilder.addContent(documentType.getName());
        }
    }
}
