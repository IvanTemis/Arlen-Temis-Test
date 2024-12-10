package com.temis.app.client;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.*;
import com.google.protobuf.ByteString;
import com.temis.app.utils.VertexAIUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ChatAIClient {

    private final VertexAI vertexAi;
    private final GenerativeModel baseModel;

    private String systemInstruction = null;

    private final CloudStorageClient cloudStorageClient;

    // Constructor para inicializar con los parámetros dinámicos
    public ChatAIClient(String projectId, String location, String modelName, CloudStorageClient cloudStorageClient) throws IOException {
        this.cloudStorageClient = cloudStorageClient;
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

        UpdatePrompt();

        // Construir el modelo generativo
        this.baseModel = new GenerativeModel.Builder()
                .setModelName(modelName)
                .setVertexAi(vertexAi)
                .setGenerationConfig(generationConfig)
                .setSafetySettings(safetySettings)
                .setSystemInstruction(ContentMaker.fromMultiModalData(systemInstruction))
                .build();
    }


    public void UpdatePrompt() throws IOException {
        log.info("Updating prompt for ChatAI...");
        systemInstruction = cloudStorageClient.ReadFile("gs://temis-storage/valanz/model/prompt.txt");
    }

    // Cerrar el cliente Vertex AI
    public void close() throws Exception {
        this.vertexAi.close();
    }

    public GenerateContentResponse sendMessage(Content message, @Nullable List<Content> history, String context) throws Exception {
        var model =  baseModel.withSystemInstruction(ContentMaker.fromMultiModalData(systemInstruction, context));

        ChatSession chatSession = model.startChat();

        if(history != null) chatSession.setHistory(history);

        return VertexAIUtils.ExponentialBackoff(10,100,10000,() -> {
            try {
                return chatSession.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, log);
    }
}