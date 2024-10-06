package com.temis.app.repository;


import com.temis.app.entity.NotaryDocumentTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotaryDocumentTypeRepository extends CrudRepository<NotaryDocumentTypeEntity, Long> {
}
