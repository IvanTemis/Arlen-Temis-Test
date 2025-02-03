package com.temis.app.service;

import com.temis.app.entity.UserEntity;

public interface SchedulerService {

    void ScheduleMessageProcessing(String phoneNumber, String messageId);

    void ScheduleDraftGeneration(String inputJson, UserEntity user) throws Exception;

    void shutdown();
}