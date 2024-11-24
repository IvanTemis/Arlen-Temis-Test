package com.temis.app.repository;


import com.temis.app.entity.MessageContextEntity;
import com.temis.app.entity.MessageResponseEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageResponseRepository extends CrudRepository<MessageResponseEntity, Long> {
}
