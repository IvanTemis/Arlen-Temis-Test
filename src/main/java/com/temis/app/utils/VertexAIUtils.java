package com.temis.app.utils;

import com.google.api.gax.rpc.ResourceExhaustedException;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FileData;
import com.google.cloud.vertexai.api.Part;
import com.google.protobuf.InvalidProtocolBufferException;
import com.temis.app.entity.VertexAiContentEntity;
import com.temis.app.model.VertexAiRole;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class VertexAIUtils {
    public static Content ContentWithDocument(String text, @Nullable String fileGsUri, @Nullable String fileMimeType){
        return ContentWithDocument(text, fileGsUri, fileMimeType, VertexAiRole.USER);
    }
    public static Content ContentWithDocument(String text, @Nullable String fileGsUri, @Nullable String fileMimeType, VertexAiRole role){
        var contentBuilder = Content.newBuilder()
                .setRole(role.name())
                .addParts(Part.newBuilder().setText(text));

        if(fileGsUri != null && fileMimeType != null){
            contentBuilder.addParts(Part.newBuilder().setFileData(
                    FileData.newBuilder()
                            .setMimeType(fileMimeType)
                            .setFileUri(fileGsUri)
            ));
        }

        return contentBuilder.build();
    }

    public static List<Content> VertexAiContentEntityToContent(List<VertexAiContentEntity> contexts) throws InvalidProtocolBufferException {
        List<Content> history = new ArrayList<>();

        for (var item : contexts) {
            List<Part> parts = new ArrayList<>();
            for (var part : item.getParts()){
                parts.add(Part.parseFrom(part));
            }

            var c = Content.newBuilder()
                    .setRole(item.getRole().name())
                    .addAllParts(parts)
                    .build();
            history.add(c);
        }

        return history;
    }

    public static <T> T ExponentialBackoff(int maxRetries, long delayMillis, long timeout, Supplier<T> action, Logger log) throws Exception {
        int retries = 0;
        long cumulativeDelay = 0;
        Exception lastException = null;
        while (true){
            try{
                if(retries >= maxRetries || cumulativeDelay > timeout){
                    if(lastException != null){
                        throw lastException;
                    }
                    else {
                        throw new InterruptedException("Interrupted Backoff without an exception.");
                    }
                }

                retries++;
                return action.get();
            }
            catch (ResourceExhaustedException e){
                lastException = e;

                double exponentialMultiplier = Math.pow(2.0, retries);

                double result = exponentialMultiplier * delayMillis;

                long millisToWait = (long) Math.min(result, Long.MAX_VALUE);

                assert millisToWait >= 0;

                cumulativeDelay += millisToWait;

                log.info("Action failed, waiting for {} milliseconds", millisToWait);
                Thread.sleep(millisToWait);
            }
        }
    }
}
