package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "notary_employee")
public class NotaryEmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @OneToOne(optional = false, targetEntity = UserEntity.class)
    UserEntity userEntity;

    @JoinColumn(name = "notary_id", nullable = false)
    @ManyToOne(optional = false, targetEntity = NotaryEntity.class)
    private NotaryEntity notary;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Timestamp lastModifiedDate;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Timestamp creationDate;

    @Column(nullable = false)
    private Boolean isActive;
}