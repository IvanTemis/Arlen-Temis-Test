package com.temis.app.dto;

import com.temis.app.entity.DocumentEntity;
import com.temis.app.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessMessagesRequest {
    private String phoneNumber;
}