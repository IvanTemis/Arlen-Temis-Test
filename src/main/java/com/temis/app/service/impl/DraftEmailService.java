package com.temis.app.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public void sendDraftByEmail(String inputJson, String draftText, String emailAddress) throws Exception {
        
        emailAddress = (emailAddress == null) ? "ivan@temislegal.ai" : emailAddress;//TO-DO Aqui no debe ir mi correo.

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(inputJson);

        String tipoSociedad = rootNode.get("tipo_sociedad").asText();
        String objetoSocial = rootNode.get("objeto_social").asText();
        int numeroSocios = rootNode.get("numero_socios").asInt();
        String tamanoEmpresa = rootNode.get("tamano_empresa").asText();
        int capitalSocial = rootNode.get("capital_social").asInt();
        String ubicacion = rootNode.get("ubicacion").asText();

        // Obtener las denominaciones
        StringBuilder denominaciones = new StringBuilder();
        for (JsonNode denominacion : rootNode.get("denominaciones")) {
            denominaciones.append(denominacion.asText()).append(", ");
        }
        if (denominaciones.length() > 0) {
            denominaciones.setLength(denominaciones.length() - 2);
        }

        // Obtener los socios
        StringBuilder sociosInfo = new StringBuilder();
        for (JsonNode socio : rootNode.get("socios")) {
            String nombre = socio.get("nombre").asText();
            String nacionalidad = socio.get("nacionalidad").asText();
            String estadoCivil = socio.get("estado_civil").asText();
            String conyuge = socio.get("conyuge").isNull() ? "N/A" : socio.get("conyuge").asText();

            sociosInfo.append("<li>")
                    .append("<strong>Nombre:</strong> ").append(nombre).append("<br />")
                    .append("<strong>Nacionalidad:</strong> ").append(nacionalidad).append("<br />")
                    .append("<strong>Estado Civil:</strong> ").append(estadoCivil).append("<br />")
                    .append("<strong>Conyuge:</strong> ").append(conyuge)
                    .append("</li>");
        }

        // Construcción del cuerpo del correo
        String subject = "Nueva Solicitud de Constitutiva de Empresas";
        String body = String.format("""
            <html>
                <body style="font-family: Arial, sans-serif; color: #333333; line-height: 1.6;">
                    <div style="border: 1px solid #dddddd; padding: 20px; max-width: 600px; margin: auto;">
                        <img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXfD8MCYZ168lxo1yiRWy3HZdZocWb3YlGk6Os-Wg34BpyGP2uyl-t8HupEDFwFcpWMOrXq2buGaa7oYEWWBWNRVEAolDrXXBY386aeg9Fs-7mLCl-VtCVWGj6PD6zh-frvJpzK1OQ?key=7b8-sBcKAi0R5Q9gtLOOytt6" alt="Valanz" style="max-width: 150px; display: block; margin: 0 auto 20px;" />
                        <p>Estimada Lic. Zélica,</p>
                        <p>A continuación te comparto una nueva solicitud de <strong>Constitutiva de Empresas</strong> que recibimos:</p>
                        <ul>
                            <li><strong>Tipo de Sociedad:</strong> %s</li>
                            <li><strong>Objeto Social:</strong> %s</li>
                            <li><strong>Número de Socios:</strong> %d</li>
                            <li><strong>Tamaño de la Empresa:</strong> %s</li>
                            <li><strong>Capital Social:</strong> $%,d</li>
                            <li><strong>Ubicación:</strong> %s</li>
                            <li><strong>Denominaciones:</strong> %s</li>
                        </ul>
                        <p><strong>Socios:</strong></p>
                        <ul>
                            %s
                        </ul>
                        <p>Te adjunto el borrador de la Constitutiva. Quedo al pendiente para cualquier duda.</p>
                        <p>Saludos,<br />Valentina</p>
                        <hr style="border: none; border-top: 1px solid #dddddd; margin: 20px 0;" />
                        <img src="https://lh7-rt.googleusercontent.com/docsz/AD_4nXfD8MCYZ168lxo1yiRWy3HZdZocWb3YlGk6Os-Wg34BpyGP2uyl-t8HupEDFwFcpWMOrXq2buGaa7oYEWWBWNRVEAolDrXXBY386aeg9Fs-7mLCl-VtCVWGj6PD6zh-frvJpzK1OQ?key=7b8-sBcKAi0R5Q9gtLOOytt6" alt="Temis Legal" style="max-width: 150px; display: block; margin: 0 auto;" />
                    </div>
                </body>
            </html>
            """, tipoSociedad, objetoSocial, numeroSocios, tamanoEmpresa, capitalSocial, ubicacion, denominaciones, sociosInfo);

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