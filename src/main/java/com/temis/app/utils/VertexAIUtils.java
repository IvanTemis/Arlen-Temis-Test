package com.temis.app.utils;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.protobuf.InvalidProtocolBufferException;
import com.temis.app.entity.VertexAiContentEntity;

import java.util.ArrayList;
import java.util.List;

public class VertexAIUtils {
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
}
