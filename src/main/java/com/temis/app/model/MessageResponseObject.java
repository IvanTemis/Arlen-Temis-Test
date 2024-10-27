package com.temis.app.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MessageResponseObject {
    String phoneNumber;
    String body;
}
