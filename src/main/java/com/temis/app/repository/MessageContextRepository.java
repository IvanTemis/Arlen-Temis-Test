package com.temis.app.repository;


import com.temis.app.entity.MessageContextEntity;
import com.temis.app.model.MessageSource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageContextRepository extends CrudRepository<MessageContextEntity, Long> {
    MessageContextEntity findFirstByPhoneNumberAndMessageSourceAndIsActiveTrueOrderByCreatedDateAsc(String phoneNumber, MessageSource messageSource);
    MessageContextEntity findFirstByPhoneNumberAndIsActiveTrueOrderByCreatedDateAsc(String phoneNumber);
}
