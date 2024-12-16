package com.temis.app.service;

import com.temis.app.entity.ServiceEntity;
import com.temis.app.entity.UserEntity;

public interface ServiceEntityService {
    ServiceEntity findActiveServiceByPhoneNumber(String phoneNumber);
    ServiceEntity saveService(ServiceEntity serviceEntity);
    void deactivateServicesForUser(UserEntity user);
}