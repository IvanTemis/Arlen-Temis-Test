package com.temis.app.service.impl;

import com.temis.app.service.EmailService;
import com.temis.app.utils.WordDocumentFormatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class DraftEmailService {

    @Autowired
    private EmailService emailService; 

    public void sendDraftByEmail(String draftText, String emailAddress) throws Exception {
        
        WordDocumentFormatter formatter = new WordDocumentFormatter();
        byte[] documentBytes = formatter.createFormattedDocument(draftText);
        
        String subject = "Borrador de alta constitutiva";
        String body = "Adjunto encontrarás el borrador de la alta constitutiva.";
        Pair<String, ByteArrayResource> attachment = Pair.of("Borrador.docx", new ByteArrayResource(documentBytes));

        //emailService.SendHtmlEmailWithAttachments(emailAddress, subject, body, attachment);
        emailService.SendHtmlEmailWithAttachments("ivan.cantu.garcia@gmail.com", subject, body, attachment);

        log.info("Borrador enviado a {}", emailAddress);
    }
}