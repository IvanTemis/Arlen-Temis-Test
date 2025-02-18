package com.temis.app.service.impl;

import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.temis.app.agent.EmailContentCreatorAgent;
import com.temis.app.entity.MessageContextEntity;
import com.temis.app.service.EmailService;

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

    @Autowired
    private EmailContentCreatorAgent emailContentCreatorAgent;

    public void sendDraftByEmailWithAttachment(String inputJson, byte[] documentBytes, String fileName, String emailAddress) throws Exception {
        emailAddress = (emailAddress == null) ? "ivan@temislegal.ai" : emailAddress; // Fallback para el correo
        
        var result = emailContentCreatorAgent.CreateEmailContent(inputJson);
        
        var email = ResponseHandler.getText(result)
                .replace("```html", "")
                .replace("```", "")
                .replace("<!DOCTYPE html>", "")
                .replace("<html>", "")
                .replace("<\\html>", "")
                .trim();
        
        log.info("Email Creator recibió \n{}\n y produjo:\n{}", inputJson, email);
        
        // Construcción del cuerpo del correo
        String subject = "Nueva Solicitud de Constitutiva de Empresas";
        String body = """
            <html>
                <body style="font-family: Arial, sans-serif; color: #333333; line-height: 1.6;">
                    <div style="border: 1px solid #dddddd; padding: 20px; max-width: 600px; margin: auto;">
                        <img src="https://your-image-url" alt="Valanz" style="max-width:100%; display: block; margin: 0 auto 20px;" />
                        <p>Estimada Lic. Zélica,</p>
                        <p>A continuación te comparto una nueva solicitud de <strong>Constitutiva de Empresas</strong> que recibimos:</p>
                       \s"""
                + email +
                """
                        <p>Te adjunto el borrador de la Constitutiva. Quedo al pendiente para cualquier duda.</p>
                        <p>Saludos,<br />Valentina</p>
                        <hr style="border: none; border-top: 1px solid #dddddd; margin: 20px 0;" />
                    </div>
                </body>
            </html>
            """;
        
        // Crear el adjunto del documento Word
        ByteArrayResource attachment = new ByteArrayResource(documentBytes);
        
        String[] bccAddresses = {
            "ivan@temislegal.ai",
            "alex@temislegal.ai",
            "diego@temislegal.ai",
            "gabriel@temislegal.ai"
        };
        
        // Enviar el correo con el documento adjunto
        emailService.SendHtmlEmailWithAttachments(
            emailAddress,
            subject,
            body,
            bccAddresses,
            Pair.of(fileName, attachment)
        );
        
        log.info("Borrador enviado a {} con adjunto Word, copia oculta a: {}", emailAddress, String.join(", ", bccAddresses));
    }
}