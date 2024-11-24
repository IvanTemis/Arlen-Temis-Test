package com.temis.app.repository;


import com.temis.app.entity.UserEntity;
import com.temis.app.entity.VertexAiContentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VertexAiContentRepository extends CrudRepository<VertexAiContentEntity, Long> {
    List<VertexAiContentEntity> findByUserEntityOrderByCreatedDateAsc(UserEntity userEntity);
}
