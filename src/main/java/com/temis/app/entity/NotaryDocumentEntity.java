package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;
import java.util.Set;

@Data
@Entity
@Table(name = "notary_document")
public class NotaryDocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "notary_document_type_id", nullable = true)
    private NotaryDocumentTypeEntity documentType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Timestamp creationDate;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(columnDefinition = "TEXT")
    private String resume;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private Timestamp lastUpdateDate;

    @Column(nullable = false)
    private Timestamp signatureDate;

    @ElementCollection
    @Column(name = "keywords")
    private Set<String> keywords;
}