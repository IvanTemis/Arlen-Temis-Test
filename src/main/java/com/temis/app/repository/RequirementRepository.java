package com.temis.app.repository;


import com.temis.app.entity.RequirementEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequirementRepository extends CrudRepository<RequirementEntity, Long> {
}
