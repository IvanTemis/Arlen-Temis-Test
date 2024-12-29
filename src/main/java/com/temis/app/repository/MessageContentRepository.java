package com.temis.app.repository;


import com.temis.app.entity.MessageContentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageContentRepository extends CrudRepository<MessageContentEntity, Long> {
}
