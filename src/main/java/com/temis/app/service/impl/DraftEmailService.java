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

        byte[] wordBytes = formatter.createFormattedDocument(draftText);
        byte[] pdfBytes = formatter.createPDFDocument(draftText);

        String subject = "Borrador de alta constitutiva";
        String body = "Adjunto encontrarás el borrador de la alta constitutiva en formatos Word y PDF.";

        Pair<String, ByteArrayResource> wordAttachment = Pair.of("Borrador.docx", new ByteArrayResource(wordBytes));
        Pair<String, ByteArrayResource> pdfAttachment = Pair.of("Borrador.pdf", new ByteArrayResource(pdfBytes));

        String[] bccAddresses = {
            "ivan@temislegal.ai",
            "alex@temislegal.ai",
            "diego@temislegal.ai",
            "gabriel@temislegal.ai"
        };

        emailService.SendHtmlEmailWithAttachments(emailAddress, subject, body, bccAddresses, wordAttachment, pdfAttachment);
        log.info("Borrador enviado a {} con adjuntos Word y PDF, copia oculta a: {}", emailAddress, String.join(", ", bccAddresses));
    }
}