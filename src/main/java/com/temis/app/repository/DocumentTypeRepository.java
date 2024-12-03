package com.temis.app.repository;


import com.temis.app.entity.DocumentEntity;
import com.temis.app.entity.DocumentTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTypeRepository extends CrudRepository<DocumentTypeEntity, Long> {
}
