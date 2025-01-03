package com.temis.app.repository;


import com.temis.app.entity.MessageContextEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageContextRepository extends CrudRepository<MessageContextEntity, Long> {
    MessageContextEntity findFirstByPhoneNumberAndIsActiveTrueOrderByCreatedDateAsc(String phoneNumber);
}
