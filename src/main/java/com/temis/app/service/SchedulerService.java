package com.temis.app.service;

public interface SchedulerService {
    void ScheduleMessageProcessing(String phoneNumber);

    void shutdown();
}