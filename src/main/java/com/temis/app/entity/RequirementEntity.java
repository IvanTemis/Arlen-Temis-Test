package com.temis.app.entity;

import com.temis.app.model.RequirementType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

import static jakarta.persistence.EnumType.STRING;

@Data
@Builder(builderMethodName = "hiddenBuilder")
@Entity
@Table(name = "requirement")
@EntityListeners(AuditingEntityListener.class)
public class RequirementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    @Enumerated(STRING)
    RequirementType requirementType;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity serviceEntity;

    @JoinColumn(nullable = true, name = "document_id")
    @OneToOne(optional = true, targetEntity = DocumentEntity.class)
    DocumentEntity documentEntity;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date lastModifiedDate;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;

    public static RequirementEntityBuilder builder(ServiceEntity service) {
        return hiddenBuilder().serviceEntity(service);
    }
}