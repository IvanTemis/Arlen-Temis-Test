package com.temis.app.repository;


import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageContextRepository extends CrudRepository<MessageContextEntity, Long> {
    List<MessageContextEntity> findByMessageId(String messageId);
}
