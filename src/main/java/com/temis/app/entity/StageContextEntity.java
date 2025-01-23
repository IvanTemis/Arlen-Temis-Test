package com.temis.app.entity;

import com.temis.app.model.ServiceStage;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

import static jakarta.persistence.EnumType.STRING;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "stage_context")
@EntityListeners(AuditingEntityListener.class)
public class StageContextEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    @Enumerated(STRING)
    ServiceStage parentStage = ServiceStage.ANY;

    @Column(nullable = false)
    @Enumerated(STRING)
    ServiceStage targetStage = ServiceStage.ANY;

    @Column(nullable = false, columnDefinition = "TEXT")
    String context;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Date createdDate;

    @Column(nullable = false)
    private Boolean isActive;

    @JoinColumn(nullable = false, name = "service_id")
    @ManyToOne(optional = false, targetEntity = ServiceEntity.class)
    ServiceEntity service;

}