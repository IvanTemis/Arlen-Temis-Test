package com.temis.app.service;

public interface MessageProcessingService {
    void ProcessAccumulatedMessages(String phoneNumber) throws Exception;
}