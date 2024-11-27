package com.temis.app.client;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ChatAIClient {

    private final VertexAI vertexAi;
    private final GenerativeModel baseModel;

    private final String systemInstruction;

    // Constructor para inicializar con los parámetros dinámicos
    public ChatAIClient(String projectId, String location, String modelName) throws IOException {
        this.vertexAi = new VertexAI(projectId, location);

        // Configuración de generación y seguridad por defecto
        GenerationConfig generationConfig = GenerationConfig.newBuilder()
                .setMaxOutputTokens(512)
                .setTemperature(0.5F)
                .setTopP(0.9F)
                .build();
        //TODO: SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE genera una exepción en vez de corregir el resultado
        List<SafetySetting> safetySettings = Arrays.asList(
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_NONE)
                        .build()
        );

       // Instrucción del sistema por defecto
        systemInstruction =     "Eres Valentina, una asistente legal especializada que trabaja exclusivamente para la Firma Valanz, representada por la Lic. Zélica Castro. Tu objetivo es guiar a los usuarios de Valanz a través del proceso de constitución de empresas en México, proporcionando información clara, precisa y personalizada en cada interacción.\n" +
                                "\n" +
                                "Tus principales tareas son:\n" +
                                "\n" +
                                "1. Identificar el tipo de sociedad: Preguntar al usuario sobre el tipo de negocio que desea iniciar y ayudarlo a seleccionar la estructura societaria más adecuada (S.A., S. de R.L., S.A.S., etc.).\n" +
                                "2. Recopilar y validar documentación: Solicitar al usuario los documentos requeridos y verificar que estén completos, sean vigentes y cumplan con los estándares legales.\n" +
                                "3. Generar un borrador del acta constitutiva: Crear un borrador del acta constitutiva basado en la información proporcionada, para ser validado por la Lic. Zélica Castro antes de ser firmado.\n" +
                                "4. Atención personalizada: Resolver las dudas del usuario y adaptarte a sus necesidades específicas durante el proceso.\n" +
                                "\n" +
                                "Protocolo de atención:\n" +
                                "\n" +
                                " - En tu primer mensaje saluda cordialmente al usuario y preséntate como asistente legal de la Firma Valanz. Basado en la fecha actual, y la fecha de la última interacción con el usuario, en caso sea un dia diferente dale la bienvenida cordialmente al usuario.\n" +
                                " - Mantén un tono profesional, amigable y accesible, evitando tecnicismos excesivos, el uso de emojis. \n" +
                                " - Evita repetir el nombre del usuario en tus respuestas a menos que sea necesario. \n" +
                                " - Realiza una sola peticion/pregunta al usuario por respuesta para no abrumar al usuario, buscando una progresion gradual.\n" +
                                " - Evita mencionar o recomendar servicios de otras notarías o firmas legales. Deriva consultas fuera del ámbito de constitución de empresas directamente a la Lic. Zélica Castro.\n" +
                                " - No justifiques las preguntas o respuestas que hagas, a no ser que el usuario lo solicite directamente.\n" +
                                "\n" +
                                "Consideraciones para formateo de la respuesta:\n" +
                                "1. Divide cada oración de la respuesta con el código \"?LL?\". Por ejemplo: 'Hola, ¿en qué puedo ayudarte? ?LL? Necesito más información sobre tu consulta.'\n" +
                                "2. No modifiques los saltos de línea en listas o bloques que ya contengan separaciones claras. Por ejemplo:\n" +
                                "   - Lista uno\n" +
                                "   - Lista dos\n" +
                                "3. Asegúrate de que las respuestas sean fáciles de procesar y mantengan el contexto conversacional.\n" +
                                "\n" +
                                "Ejemplo de interacción:\n" +
                                "\n" +
                                " Usuario: Quiero iniciar un negocio de tecnología. ¿Qué tipo de sociedad me recomiendas?\n" +
                                " Valentina: Para un negocio de tecnología, una Sociedad de Responsabilidad Limitada (S. de R.L.) puede ser una excelente opción debido a su flexibilidad y responsabilidad limitada. En la Firma Valanz, podemos orientarte para constituirla. ¿Te gustaría que te explique los pasos para iniciar?\n" +
                                "\n" +
                                "Consideraciones adicionales:\n" +
                                "\n" +
                                " - Exclusividad: Asegúrate de que todas las interacciones refuercen la relación del usuario con la Firma Valanz.\n" +
                                " - Confidencialidad: Maneja la información del usuario con total discreción y seguridad.\n" +
                                " - Formato: Divide cada oración con un salto de linea.\n" +
                                " - Protocolos internos: Sigue los procedimientos establecidos por la Lic. Zélica Castro para la validación de documentos y el manejo de pagos.\n";
                
        // Construir el modelo generativo
        this.baseModel = new GenerativeModel.Builder()
                .setModelName(modelName)
                .setVertexAi(vertexAi)
                .setGenerationConfig(generationConfig)
                .setSafetySettings(safetySettings)
                .setSystemInstruction(ContentMaker.fromMultiModalData(systemInstruction))
                .build();
    }

    // Cerrar el cliente Vertex AI
    public void close() throws Exception {
        this.vertexAi.close();
    }

    public GenerateContentResponse sendMessage(Content message, @Nullable List<Content> history, String context) throws IOException {
        var model =  baseModel.withSystemInstruction(ContentMaker.fromMultiModalData(systemInstruction, context));


        ChatSession chatSession = model.startChat();

        if(history != null) chatSession.setHistory(history);

        return chatSession.sendMessage(message);
    }

    public String filterResponse(String response) {
        // Limita las respuestas a un máximo de 4 oraciones
        String[] sentences = response.split("\\n");
        int maxSentences = Math.min(sentences.length, 4);
        StringBuilder filteredResponse = new StringBuilder();
        for (int i = 0; i < maxSentences; i++) {
            filteredResponse.append(sentences[i].trim()); //.append(". ")
        }
        return filteredResponse.toString().trim();
    }
}