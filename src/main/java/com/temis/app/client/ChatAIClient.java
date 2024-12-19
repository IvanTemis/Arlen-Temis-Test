package com.temis.app.client;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.*;
import com.temis.app.utils.VertexAIUtils;
import lombok.extern.slf4j.Slf4j;

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

    public ChatAIClient(String projectId, String location, String modelName, CloudStorageClient cloudStorageClient, String agentId) throws IOException {
        this.cloudStorageClient = cloudStorageClient;
        this.vertexAi = new VertexAI(projectId, location);

        GenerationConfig generationConfig = GenerationConfig.newBuilder()
                .setMaxOutputTokens(512)
                .setTemperature(0.5F)
                .setTopP(0.9F)
                .build();

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

        UpdatePrompt(agentId);

        this.baseModel = new GenerativeModel.Builder()
                .setModelName(modelName)
                .setVertexAi(vertexAi)
                .setGenerationConfig(generationConfig)
                .setSafetySettings(safetySettings)
                .setSystemInstruction(ContentMaker.fromMultiModalData(systemInstruction))
                .build();
    }


    public void UpdatePrompt(String agentId) throws IOException {
        log.info("Updating prompt for agent: {}", agentId);
        String promptPath = String.format("gs://temis-storage/prompts/%s.txt", agentId);
        this.systemInstruction = cloudStorageClient.ReadFile(promptPath);
    }

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