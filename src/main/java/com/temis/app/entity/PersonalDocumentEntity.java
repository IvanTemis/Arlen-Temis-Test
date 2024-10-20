package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "personal_document")
public class PersonalDocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "personal_document_type_id", nullable = false)
    private PersonalDocumentTypeEntity documentType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Timestamp issueDate;

    @Column(nullable = false)
    private Timestamp expirationDate;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private Timestamp receptionDate;

    @Column(nullable = false)
    private Timestamp lastUpdateDate;
}