package com.temis.app.repository;


import com.temis.app.entity.ServiceEntity;
import com.temis.app.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends CrudRepository<ServiceEntity, Long> {
    ServiceEntity findFirstByPhoneNumberAndIsActiveTrue(String phoneNumber);
    List<ServiceEntity> findByUser(UserEntity user);
}
