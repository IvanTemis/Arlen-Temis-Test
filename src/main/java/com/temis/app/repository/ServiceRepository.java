package com.temis.app.repository;


import com.temis.app.entity.ServiceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends CrudRepository<ServiceEntity, Long> {
    ServiceEntity findFirstByPhoneNumberAndIsActiveTrue(String phoneNumber);
}
