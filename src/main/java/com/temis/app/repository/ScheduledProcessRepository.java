package com.temis.app.repository;


import com.temis.app.entity.ScheduledProcessEntity;
import com.temis.app.model.ScheduledProcessSchedulerType;
import com.temis.app.model.ScheduledProcessState;
import com.temis.app.model.ScheduledProcessType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledProcessRepository extends CrudRepository<ScheduledProcessEntity, Long> {
    List<ScheduledProcessEntity> findByParentAndStateAndTypeAndSchedulerTypeOrderByCreatedDateAsc(String parent, ScheduledProcessState state, ScheduledProcessType type, ScheduledProcessSchedulerType schedulerType);
    Optional<ScheduledProcessEntity> findByNameEndingWithAndTypeAndSchedulerType(String name, ScheduledProcessType type, ScheduledProcessSchedulerType schedulerType );
}
