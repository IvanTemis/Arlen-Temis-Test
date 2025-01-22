package com.temis.app.dto;

import com.temis.app.entity.DocumentEntity;
import com.temis.app.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessMessagesRequest {
    private String phoneNumber;
    private String messageId;
}