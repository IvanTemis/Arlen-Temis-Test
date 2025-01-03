package com.temis.app.repository;


import com.temis.app.entity.MessageContextContentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageContextContentRepository extends CrudRepository<MessageContextContentEntity, Long> {
}
