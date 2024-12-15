package com.temis.app.state;

import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import com.temis.app.repository.MessageContextRepository;
import com.temis.app.repository.UserRepository;
import com.temis.app.state.with_user.AdminCommandState;
import com.temis.app.state.with_user.BeginDocumentCreationState;
import com.temis.app.state.with_user.ClientVirtualAssistantState;
import com.temis.app.state.with_user.ProcessFileIntransitableState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;

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
