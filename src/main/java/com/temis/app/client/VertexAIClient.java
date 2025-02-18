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
public class VertexAIClient {

    private final VertexAI vertexAi;
    private final GenerativeModel baseModel;

    public VertexAIClient(String projectId, String location, String modelName) {
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

        this.baseModel = new GenerativeModel.Builder()
                .setModelName(modelName)
                .setVertexAi(vertexAi)
                .setGenerationConfig(generationConfig)
                .setSafetySettings(safetySettings)
                .build();
    }

    public void close() throws Exception {
        this.vertexAi.close();
    }

    public GenerateContentResponse sendMessage(Content message, @Nullable List<Content> history, String prompt) throws Exception {
        var model =  baseModel.withSystemInstruction(ContentMaker.fromMultiModalData(prompt));

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

    public ResponseStream<GenerateContentResponse> startStreaming(Content message, String prompt) throws Exception {
        var model = baseModel.withSystemInstruction(ContentMaker.fromMultiModalData(prompt));
    
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
