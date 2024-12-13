package com.temis.app.service;

public interface MessageProcessingService {

    void accumulateMessage(String phoneNumber, String message);

    void scheduleMessageProcessing(String phoneNumber);

    void shutdown();
}