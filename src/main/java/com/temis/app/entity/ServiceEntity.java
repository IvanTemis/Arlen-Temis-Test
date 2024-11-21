package com.temis.app.entity;

import com.temis.app.model.RequirementType;
import com.temis.app.model.ServiceState;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.sql.Timestamp;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;

@Data
@Builder(builderMethodName = "hiddenBuilder")
@Entity
@Table(name = "service")
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
    private Integer priority = 0;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = RequirementEntity.class)
    private List<RequirementEntity> requirementEntities;

    @JoinColumn(name = "employee_id", nullable = false)
    @ManyToOne(optional = false, targetEntity = NotaryEmployeeEntity.class)
    NotaryEmployeeEntity employeeEntity;

    @JoinColumn(name = "notary_id", nullable = false)
    @ManyToOne(optional = false, targetEntity = NotaryEntity.class)
    private NotaryEntity notary;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Timestamp creationDate;

    @Column(nullable = false)
    private Boolean isActive;

    public static ServiceEntityBuilder builder(NotaryEmployeeEntity employee) {
        return hiddenBuilder().employeeEntity(employee).notary(employee.getNotary());
    }
}