package com.temis.app.service;

import com.temis.app.entity.ServiceEntity;

public interface ServiceEntityService {
    ServiceEntity findActiveServiceByPhoneNumber(String phoneNumber);
    ServiceEntity saveService(ServiceEntity serviceEntity);
}