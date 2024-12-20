package com.temis.app.repository;


import com.temis.app.entity.StageContextEntity;
import com.temis.app.entity.ServiceEntity;
import com.temis.app.model.ServiceStage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StageContextRepository extends CrudRepository<StageContextEntity, Long> {
    List<StageContextEntity> findByServiceAndTargetStage(ServiceEntity service, ServiceStage targetStage);
}
