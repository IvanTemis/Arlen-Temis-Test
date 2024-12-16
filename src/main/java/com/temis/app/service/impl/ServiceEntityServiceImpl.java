package com.temis.app.service.impl;

import com.temis.app.entity.ServiceEntity;
import com.temis.app.entity.UserEntity;
import com.temis.app.repository.ServiceRepository;
import com.temis.app.service.ServiceEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceEntityServiceImpl implements ServiceEntityService {

    private final ServiceRepository serviceEntityRepository;

    @Override
    public ServiceEntity findActiveServiceByPhoneNumber(String phoneNumber) {
        return serviceEntityRepository.findFirstByPhoneNumberAndIsActiveTrue(phoneNumber);
    }
    @Override
    public ServiceEntity saveService(ServiceEntity serviceEntity) {
        return serviceEntityRepository.save(serviceEntity);
}

    @Override
    public void deactivateServicesForUser(UserEntity user) {
        var services = serviceEntityRepository.findByUser(user);
        serviceEntityRepository.deleteAll(services);
    }

}