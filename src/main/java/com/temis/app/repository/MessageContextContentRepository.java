package com.temis.app.repository;

import com.temis.app.entity.MessageContextContentEntity;
import com.temis.app.entity.MessageContextEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MessageContextContentRepository extends CrudRepository<MessageContextContentEntity, Long> {
    List<MessageContextContentEntity> findByMessageId(String messageId);
}

