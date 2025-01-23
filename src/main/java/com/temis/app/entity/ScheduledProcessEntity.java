package com.temis.app.entity;

import com.temis.app.model.ScheduledProcessSchedulerType;
import com.temis.app.model.ScheduledProcessState;
import com.temis.app.model.ScheduledProcessType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

import static jakarta.persistence.EnumType.STRING;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scheduled_process")
@EntityListeners(AuditingEntityListener.class)
public class ScheduledProcessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String parent;

    @Column(nullable = false)
    @Enumerated(STRING)
    ScheduledProcessState state;

    @Column(nullable = false)
    @Enumerated(STRING)
    ScheduledProcessType type;

    @Column(nullable = false)
    @Enumerated(STRING)
    ScheduledProcessSchedulerType schedulerType;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date lastModifiedDate;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;
}