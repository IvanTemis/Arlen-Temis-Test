package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "personal_document")
public class PersonalDocumentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(unique = true, nullable = false)
    Long Id;

    @ManyToOne
    @JoinColumn(name = "personal_document_type_id")
    PersonalDocumentTypeEntity documentType;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String path;

    @Column(nullable = true)
    String notes;

    @Column(nullable = false)
    Timestamp issueDate;

    @Column(nullable = false)
    Timestamp receptionDate;

    @Column(nullable = false)
    Timestamp expirationDate;

    @Column(nullable = false)
    Timestamp lastUpdateDate;

    @Column(nullable = false)
    boolean isActive;
}
