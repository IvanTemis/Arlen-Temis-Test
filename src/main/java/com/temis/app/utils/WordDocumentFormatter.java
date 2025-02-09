package com.temis.app.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WordDocumentFormatter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Patrones para identificar los marcadores
    private static final String VARIABLE_PATTERN = "\\{\\{([\\w.]+)\\}\\}";
    private static final String LIST_PATTERN = "\\{\\{LIST ([\\w.]+)\\}\\}";
    private static final String BLOCK_PATTERN = "\\{\\{#EACH ([\\w.]+)\\}\\}([\\s\\S]*?)\\{\\{/EACH\\}\\}";
    // Patrón IF que permite espacios y saltos de línea alrededor del contenido.
    private static final String IF_PATTERN = "(?s)\\{\\{#IF\\s+([\\w.]+)\\}\\}\\s*(.*?)\\s*\\{\\{\\/IF\\}\\}";
    // Patrón para marcadores indexados: FIRST, SECOND, THIRD, FOURTH, FIFTH, LAST.
    private static final String INDEXED_PATTERN = "\\{\\{(FIRST|SECOND|THIRD|FOURTH|FIFTH|LAST) ([\\w.]+)\\}\\}";

    public byte[] formatDocument(byte[] template, String jsonData) throws Exception {
        log.info("Starting document formatting with JSON: {}", jsonData);
        JsonNode rootNode = objectMapper.readTree(jsonData);

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(template))) {
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                processParagraph(paragraph, rootNode);
            }
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                doc.write(out);
                return out.toByteArray();
            }
        }
    }

    /**
     * Une los runs de un párrafo, procesa (IF, EACH, INDEXED, LIST y variables) y actualiza el párrafo.
     */
    private void processParagraph(XWPFParagraph paragraph, JsonNode data) {
        String mergedText = mergeRuns(paragraph);
        if (mergedText == null || mergedText.trim().isEmpty()) return;

        log.debug("Processing paragraph: {}", mergedText);

        // Procesar el texto en el orden deseado:
        // 1. Condicionales (IF)
        // 2. Bloques iterativos (EACH)
        // 3. Marcadores indexados (FIRST, SECOND, etc.)
        // 4. Marcadores LIST
        // 5. Variables simples
        String processedText = processText(mergedText, data);
        updateParagraphText(paragraph, processedText);
    }

    private String processText(String text, JsonNode data) {
        String processed = text;

        processed = processConditionals(processed, data);
        processed = processIterativeBlocks(processed, data);
        processed = processIndexedPlaceholders(processed, data);
        processed = processLists(processed, data);
        processed = processVariables(processed, data);

        return processed;
    }

    /**
     * Une el texto de todos los runs de un párrafo en un solo String.
     */
    private String mergeRuns(XWPFParagraph paragraph) {
        StringBuilder sb = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null) {
                sb.append(text);
            }
        }
        return sb.toString();
    }

    /**
     * Procesa los bloques iterativos definidos con el patrón:
     * {{#EACH ruta}} contenido {{/EACH}}
     * Dentro de cada bloque, se procesan primero las condicionales (IF),
     * luego las listas, luego las variables y también los marcadores indexados.
     * Se unen las iteraciones usando "\n" para saltos de línea.
     */
    private String processIterativeBlocks(String text, JsonNode data) {
        Pattern pattern = Pattern.compile(BLOCK_PATTERN);
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String arrayPath = matcher.group(1);
            String blockTemplate = matcher.group(2);
            JsonNode arrayNode = getNodeByPath(data, arrayPath);

            String replacement = "";
            if (arrayNode != null && arrayNode.isArray()) {
                List<String> blocks = new ArrayList<>();
                for (JsonNode item : arrayNode) {
                    String processedBlock = processConditionals(blockTemplate, item);
                    processedBlock = processLists(processedBlock, item);
                    processedBlock = processVariables(processedBlock, item);
                    processedBlock = processIndexedPlaceholders(processedBlock, item);
                    blocks.add(processedBlock);
                }
                replacement = String.join("\n", blocks);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Procesa los marcadores indexados (FIRST, SECOND, THIRD, FOURTH, FIFTH, LAST).
     * Por ejemplo: {{FIRST socios.nombre_completo}}
     */
    private String processIndexedPlaceholders(String text, JsonNode data) {
        Pattern pattern = Pattern.compile(INDEXED_PATTERN);
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String positionStr = matcher.group(1).toUpperCase();
            String path = matcher.group(2);
            int index;
            switch (positionStr) {
                case "FIRST":
                    index = 0;
                    break;
                case "SECOND":
                    index = 1;
                    break;
                case "THIRD":
                    index = 2;
                    break;
                case "FOURTH":
                    index = 3;
                    break;
                case "FIFTH":
                    index = 4;
                    break;
                case "LAST":
                    index = -1;
                    break;
                default:
                    index = 0;
            }
            String replacement = "";
            JsonNode node = getIndexedValue(data, path, index);
            if (node != null) {
                replacement = node.asText();
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Método auxiliar para obtener el valor indexado de un nodo.
     * Se espera que la ruta tenga la forma "arrayProp.subProp1.subProp2..."
     * donde "arrayProp" es un array. Se obtiene el elemento en la posición indicada y se recorre
     * el resto de la ruta.
     *
     * @param root  Nodo raíz donde buscar.
     * @param path  Ruta separada por puntos.
     * @param index Índice deseado (0 para FIRST, 1 para SECOND, …; -1 para LAST).
     * @return El nodo encontrado o null.
     */
    private JsonNode getIndexedValue(JsonNode root, String path, int index) {
        String[] parts = path.split("\\.");
        if (parts.length == 0) return null;
        JsonNode currentNode = root.get(parts[0]);
        if (currentNode != null && currentNode.isArray()) {
            int arrayIndex = (index == -1) ? currentNode.size() - 1 : index;
            if (currentNode.size() > arrayIndex && arrayIndex >= 0) {
                currentNode = currentNode.get(arrayIndex);
                // Recorrer el resto de la ruta, si existe.
                for (int i = 1; i < parts.length; i++) {
                    if (currentNode != null) {
                        currentNode = currentNode.get(parts[i]);
                    } else {
                        break;
                    }
                }
                return currentNode;
            }
        } else {
            // Si no es un array, intentamos la ruta completa.
            return getNodeByPath(root, path);
        }
        return null;
    }

    /**
     * Procesa los marcadores LIST.
     */
    private String processLists(String text, JsonNode data) {
        Pattern pattern = Pattern.compile(LIST_PATTERN);
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String path = matcher.group(1);
            String replacement = "";
            String[] parts = path.split("\\.");
            JsonNode firstNode = data.get(parts[0]);
            if (firstNode != null && firstNode.isArray()) {
                List<String> items = new ArrayList<>();
                for (JsonNode item : firstNode) {
                    JsonNode fieldValue = item;
                    for (int i = 1; i < parts.length; i++) {
                        if (fieldValue != null) {
                            fieldValue = fieldValue.get(parts[i]);
                        }
                    }
                    if (fieldValue != null) {
                        items.add(fieldValue.asText());
                    }
                }
                replacement = String.join(", ", items);
            } else {
                JsonNode fullNode = getNodeByPath(data, path);
                if (fullNode != null && fullNode.isArray()) {
                    List<String> items = new ArrayList<>();
                    for (JsonNode item : fullNode) {
                        items.add(item.asText());
                    }
                    replacement = String.join(", ", items);
                } else if (fullNode != null) {
                    replacement = fullNode.asText();
                }
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Procesa los marcadores de variables simples.
     */
    private String processVariables(String text, JsonNode data) {
        Pattern pattern = Pattern.compile(VARIABLE_PATTERN);
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String path = matcher.group(1);
            JsonNode value = getNodeByPath(data, path);
            String replacement = value != null ? value.asText() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Procesa los bloques condicionales (IF).
     */
    private String processConditionals(String text, JsonNode data) {
        Pattern pattern = Pattern.compile(IF_PATTERN);
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String conditionPath = matcher.group(1);
            String blockContent = matcher.group(2);
            JsonNode conditionNode = getNodeByPath(data, conditionPath);
            if (conditionNode != null && !conditionNode.asText().trim().isEmpty()) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(blockContent));
            } else {
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Obtiene un nodo del JSON usando una ruta separada por puntos.
     * Por ejemplo: getNodeByPath(root, "datos_generales.dia")
     */
    private JsonNode getNodeByPath(JsonNode root, String path) {
        String[] parts = path.split("\\.");
        JsonNode currentNode = root;
        for (String part : parts) {
            if (currentNode != null) {
                currentNode = currentNode.get(part);
            } else {
                break;
            }
        }
        return currentNode;
    }

    /**
     * Actualiza el párrafo con el nuevo texto.
     * Se elimina el contenido anterior y se añade el nuevo en distintos runs, uno por cada línea.
     * Se fuerza la fuente a Arial, pero se copian las propiedades (tamaño, negrita, cursiva y color)
     * del primer run original (capturadas en variables locales) para respetar el formato.
     * Los saltos de línea se agregan con run.addBreak().
     */
    private void updateParagraphText(XWPFParagraph paragraph, String newText) {
        // Capturar propiedades del primer run antes de borrar.
        int fontSize = -1;
        boolean isBold = false;
        boolean isItalic = false;
        String color = null;
        if (paragraph.getRuns() != null && !paragraph.getRuns().isEmpty()) {
            XWPFRun firstRun = paragraph.getRuns().get(0);
            fontSize = firstRun.getFontSize(); // Retorna -1 si no configurado.
            isBold = firstRun.isBold();
            isItalic = firstRun.isItalic();
            color = firstRun.getColor();
        }
        // Eliminar los runs existentes.
        clearParagraph(paragraph);
        // Dividir el nuevo texto en líneas (se respeta \n)
        String[] lines = newText.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            XWPFRun run = paragraph.createRun();
            run.setFontFamily("Arial"); // Forzar fuente a Arial.
            if (fontSize != -1) {
                run.setFontSize(fontSize);
            }
            run.setBold(isBold);
            run.setItalic(isItalic);
            if (color != null) {
                run.setColor(color);
            }
            run.setText(lines[i]);
            if (i < lines.length - 1) {
                run.addBreak();
            }
        }
    }

    /**
     * Elimina todos los runs de un párrafo.
     */
    private void clearParagraph(XWPFParagraph paragraph) {
        for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }
    }
}