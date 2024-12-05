package com.temis.app.repository;


import com.temis.app.entity.DocumentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends CrudRepository<DocumentEntity, Long> {
}
