package com.temis.app.dto;

import com.temis.app.entity.DocumentEntity;
import com.temis.app.entity.UserEntity;
import lombok.Data;

@Data
public class IncorporationRequest {
    private DocumentEntity document;
    private String context;
    private UserEntity user;
}