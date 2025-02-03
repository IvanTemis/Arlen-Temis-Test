package com.temis.app.utils;

import org.apache.poi.xwpf.usermodel.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WordDocumentFormatter {

    public byte[] createFormattedDocument(byte[] templateBytes, String jsonData) throws Exception {
        log.info("CreateFormattedDocument function called");
        // Parsear el JSON a un mapa
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = objectMapper.readValue(jsonData, Map.class);

        // Leer el contenido del machote como un documento Word
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(templateBytes))) {
            
            // Reemplazar marcadores en todos los párrafos
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                String processedText = replacePlaceholders(text, data);

                // Eliminar el contenido actual y escribir el nuevo texto
                clearParagraph(paragraph);
                XWPFRun run = paragraph.createRun();
                run.setText(processedText);
            }

            // Guardar el documento en un ByteArrayOutputStream
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                document.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private void clearParagraph(XWPFParagraph paragraph) {
        int runs = paragraph.getRuns().size();
        for (int i = 0; i < runs; i++) {
            paragraph.removeRun(0);
        }
    }

    private String replacePlaceholders(String text, Map<String, Object> data) {
        log.info("ReplacePlaceholders function called");
        for (String key : data.keySet()) {
            Object value = data.get(key);
    
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) value;
                for (String subKey : subMap.keySet()) {
                    String placeholder = String.format("[{%s.%s}]", key, subKey);
                    text = text.replace(placeholder, subMap.get(subKey) != null ? subMap.get(subKey).toString() : "");
                }
            } else if (value instanceof Iterable) {
                // Procesar listas con formato específico
                text = processListPlaceholders(text, key, (Iterable<?>) value);
            } else {
                String placeholder = String.format("[{%s}]", key);
                text = text.replace(placeholder, value != null ? value.toString() : "");
            }
        }
        return text;
    }

    private String processListPlaceholders(String text, String key, Iterable<?> list) {
        // Buscar marcadores con formatos específicos (con o sin subcampo)
        log.info("ProcessListPlaceholders function called");
        String regex = String.format("\\[\\{%s(\\.[a-zA-Z0-9_]+)?\\,([A-Z]+)\\}\\]", key); // Ejemplo: [{denominaciones,COMMA}]
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
    
        while (matcher.find()) {
            String subfield = matcher.group(1); // Subcampo (puede ser null)
            String format = matcher.group(2);  // Formato (e.g., COMMA, BULLET, LINE)
    
            // Generar el texto reemplazado
            String replacement = formatList(list, subfield != null ? subfield.substring(1) : null, format);
            text = text.replace(matcher.group(0), replacement);
        }
    
        return text;
    }

    private String formatList(Iterable<?> list, String subfield, String format) {
        log.info("FormatList function called");
        StringBuilder formattedList = new StringBuilder();
    
        switch (format) {
            case "COMMA":
                for (Object item : list) {
                    String value = extractSubfield(item, subfield);
                    formattedList.append(value).append(", ");
                }
                if (formattedList.length() > 0) {
                    formattedList.setLength(formattedList.length() - 2); // Eliminar la última coma y espacio
                }
                break;
    
            case "BULLET":
                for (Object item : list) {
                    String value = extractSubfield(item, subfield);
                    formattedList.append("• ").append(value).append("\n");
                }
                break;
    
            case "LINE":
                for (Object item : list) {
                    String value = extractSubfield(item, subfield);
                    formattedList.append(value).append("\n");
                }
                break;
    
            default:
                throw new IllegalArgumentException("Formato de lista no soportado: " + format);
        }
    
        return formattedList.toString();
    }

    private String extractSubfield(Object item, String subfield) {
        if (item instanceof Map && subfield != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> itemMap = (Map<String, Object>) item;
            return itemMap.getOrDefault(subfield, "").toString();
        }
        return item.toString(); // Si no hay subcampo, usar el valor completo
    }
}