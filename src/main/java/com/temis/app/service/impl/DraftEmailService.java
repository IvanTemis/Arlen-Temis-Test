package com.temis.app.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.temis.app.client.EmailContentCreatorClient;
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

    @Autowired
    private EmailContentCreatorClient emailContentCreatorClient;

    public void sendDraftByEmail(String inputJson, String draftText, String emailAddress) throws Exception {
        
        emailAddress = (emailAddress == null) ? "ivan@temislegal.ai" : emailAddress;//TO-DO Aqui no debe ir mi correo.

        var result = emailContentCreatorClient.CreateEmailContent(inputJson);

        // Construcción del cuerpo del correo
        String subject = "Nueva Solicitud de Constitutiva de Empresas";
        String body = String.format("""
            <html>
                <body style="font-family: Arial, sans-serif; color: #333333; line-height: 1.6;">
                    <div style="border: 1px solid #dddddd; padding: 20px; max-width: 600px; margin: auto;">
                        <img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXfD8MCYZ168lxo1yiRWy3HZdZocWb3YlGk6Os-Wg34BpyGP2uyl-t8HupEDFwFcpWMOrXq2buGaa7oYEWWBWNRVEAolDrXXBY386aeg9Fs-7mLCl-VtCVWGj6PD6zh-frvJpzK1OQ?key=7b8-sBcKAi0R5Q9gtLOOytt6" alt="Valanz" style="max-width:100%; display: block; margin: 0 auto 20px;" />
                        <p>Estimada Lic. Zélica,</p>
                        <p>A continuación te comparto una nueva solicitud de <strong>Constitutiva de Empresas</strong> que recibimos:</p>
                        %s
                        <p>Te adjunto el borrador de la Constitutiva. Quedo al pendiente para cualquier duda.</p>
                        <p>Saludos,<br />Valentina</p>
                        <hr style="border: none; border-top: 1px solid #dddddd; margin: 20px 0;" />
                        <img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXfD8MCYZ168lxo1yiRWy3HZdZocWb3YlGk6Os-Wg34BpyGP2uyl-t8HupEDFwFcpWMOrXq2buGaa7oYEWWBWNRVEAolDrXXBY386aeg9Fs-7mLCl-VtCVWGj6PD6zh-frvJpzK1OQ?key=7b8-sBcKAi0R5Q9gtLOOytt6" alt="Temis Legal" style="max-width:100%; display: block; margin: 0 auto;" />
                    </div>
                </body>
            </html>
            """, ResponseHandler.getText(result).trim());

        // Simular la creación de archivos Word y PDF
        WordDocumentFormatter formatter = new WordDocumentFormatter();
        byte[] wordBytes = formatter.createFormattedDocument(draftText);
        //byte[] pdfBytes = formatter.createPDFDocument(draftText);

        Pair<String, ByteArrayResource> wordAttachment = Pair.of("Borrador.docx", new ByteArrayResource(wordBytes));
        //Pair<String, ByteArrayResource> pdfAttachment = Pair.of("Borrador.pdf", new ByteArrayResource(pdfBytes));

        String[] bccAddresses = {
            "ivan@temislegal.ai",
            "alex@temislegal.ai",
            "diego@temislegal.ai",
            "gabriel@temislegal.ai"
        };

        emailService.SendHtmlEmailWithAttachments(emailAddress, subject, body, bccAddresses, wordAttachment);
        log.info("Borrador enviado a {} con adjuntos Word y PDF, copia oculta a: {}", emailAddress, String.join(", ", bccAddresses));
    }
}