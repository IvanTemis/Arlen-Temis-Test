package com.temis.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class File {

    private String id;
    private String source;
    private String mediaUrl;
    private String mediaType;
    private String senderInfo;
    private String profileName;
    private String messageId;
}