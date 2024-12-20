package com.temis.app.entity;

import com.temis.app.model.ServiceStage;
import com.temis.app.model.ServiceState;
import jakarta.persistence.*;
import lombok.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder(builderMethodName = "hiddenBuilder")
@Table(name = "service")
@EntityListeners(AuditingEntityListener.class)
public class ServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(STRING)
    ServiceState serviceState = ServiceState.PENDING;

    @Column(nullable = false)
    @Enumerated(STRING)
    ServiceStage serviceStage = ServiceStage.UNKNOWN;

    @Column(nullable = false)
    private Integer priority = 0;

    @OneToMany(mappedBy = "serviceEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequirementEntity> requirementEntities;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Date creationDate;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, unique = false)
    private String phoneNumber;

    @Setter
    @Nullable
    @JoinColumn(nullable = true, name = "user_id")
    @ManyToOne(optional = true, targetEntity = UserEntity.class)
    UserEntity user;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StageContextEntity> stageContextEntities;

}