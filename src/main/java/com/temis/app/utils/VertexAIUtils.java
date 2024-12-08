package com.temis.app.utils;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FileData;
import com.google.cloud.vertexai.api.Part;
import com.google.protobuf.InvalidProtocolBufferException;
import com.temis.app.entity.VertexAiContentEntity;
import com.temis.app.model.VertexAiRole;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
}
