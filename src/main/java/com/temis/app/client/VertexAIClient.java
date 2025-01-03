package com.temis.app.client;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class VertexAIClient {

    private final String projectId;
    private final String location;
    private final String modelName;
    private final VertexAI vertexAi;
    private GenerativeModel model;

    // Constructor para inicializar con los parámetros dinámicos
    public VertexAIClient(String projectId, String location, String modelName) throws IOException {
        this.projectId = projectId;
        this.location = location;
        this.modelName = modelName;
        this.vertexAi = new VertexAI(projectId, location);

        // Configuración de generación y seguridad por defecto
        GenerationConfig generationConfig = GenerationConfig.newBuilder()
                .setMaxOutputTokens(2048)
                .setTemperature(0.2F)
                .setTopP(0.95F)
                .build();

        List<SafetySetting> safetySettings = Arrays.asList(
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                        .build(),
                SafetySetting.newBuilder()
                        .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                        .build()
        );

        // Instrucción del sistema por defecto
        String systemInstruction = "Eres un bot que resume documentos legales para una notaría. Tu tarea es que los notarios comprendan más rápidamente lo más relevante de contratos o documentos. Tu trabajo es resumir los textos que te sean enviados.\n\nAsegúrate de:\n* Mantener tus resúmenes por debajo de 400 palabras\n* Incluir un título en negrita con cada resumen\n* Centrarte en los puntos principales del texto\n* Mantenlo condensado y al grano\n* No alucinar";

        // Construir el modelo generativo
        this.model = new GenerativeModel.Builder()
                .setModelName(modelName)
                .setVertexAi(vertexAi)
                .setGenerationConfig(generationConfig)
                .setSafetySettings(safetySettings)
                .setSystemInstruction(ContentMaker.fromMultiModalData(systemInstruction))
                .build();
    }

    // Método para generar el resumen basado en el texto enviado
    public String summarize(String documentText) throws IOException {
        // Crear el contenido para el modelo
        var content = ContentMaker.fromMultiModalData(documentText);

        // Generar el contenido
        ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(content);

        // Obtener y devolver la respuesta del modelo (puedes cambiar esto si quieres manejar las respuestas de otra manera)
        StringBuilder result = new StringBuilder();
        responseStream.stream().forEach(response -> result.append(response.toString()).append("\n"));
        return result.toString();
    }

    // Cerrar el cliente Vertex AI
    public void close() throws Exception {
        this.vertexAi.close();
    }
}