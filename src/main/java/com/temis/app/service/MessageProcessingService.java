package com.temis.app.service;

public interface MessageProcessingService {

    void scheduleMessageProcessing(String phoneNumber);

    void shutdown();
}