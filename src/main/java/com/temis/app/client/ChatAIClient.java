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
    private final String agentId;

    private String systemInstruction = null;

    private final CloudStorageClient cloudStorageClient;

    public ChatAIClient(String projectId, String location, String modelName, CloudStorageClient cloudStorageClient, String agentId) throws IOException {
        this.cloudStorageClient = cloudStorageClient;
        this.agentId = agentId;
        this.vertexAi = new VertexAI(projectId, location);

        GenerationConfig generationConfig = GenerationConfig.newBuilder()
                .setMaxOutputTokens(2048)
                .setTemperature(0.5F)
                .setTopP(0.9F)
                .setTopK(1F)
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

        UpdatePrompt();

        this.baseModel = new GenerativeModel.Builder()
                .setModelName(modelName)
                .setVertexAi(vertexAi)
                .setGenerationConfig(generationConfig)
                .setSafetySettings(safetySettings)
                .setSystemInstruction(ContentMaker.fromMultiModalData(systemInstruction))
                .build();
    }


    public void UpdatePrompt() throws IOException {
        log.info("Updating prompt for agent: {}", agentId);
        String promptPath = String.format("gs://temis-prd-storage/prompts/%s.txt", agentId);
        this.systemInstruction = cloudStorageClient.ReadFile(promptPath);
    }

    public void close() throws Exception {
        this.vertexAi.close();
    }

    public GenerateContentResponse sendMessage(Content message, @Nullable List<Content> history, String context) throws Exception {
        var model =  baseModel.withSystemInstruction(ContentMaker.fromMultiModalData(systemInstruction, context));

        ChatSession chatSession = model.startChat();

        if(history != null) chatSession.setHistory(history);

        return VertexAIUtils.ExponentialBackoff(10,1000,10000,() -> {
            try {
                return chatSession.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, log);
    }

    public ResponseStream<GenerateContentResponse> startStreaming(Content message, String context) throws Exception {
        var model = baseModel.withSystemInstruction(ContentMaker.fromMultiModalData(systemInstruction, context));
    
        ChatSession chatSession = model.startChat();
    
        return VertexAIUtils.ExponentialBackoff(10, 1000, 10000, () -> {
            try {
                return chatSession.sendMessageStream(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, log);
    }
}
