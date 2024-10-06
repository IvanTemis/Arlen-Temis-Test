package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "notary_document")
public class NotaryDocumentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(unique = true, nullable = false)
    Long Id;

    @ManyToOne
    @JoinColumn(name = "notary_document_type_id")
    NotaryDocumentTypeEntity documentType;

    @ManyToOne
    @JoinColumn(name = "notary_id")
    NotaryEntity notary;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String path;

    @Column(nullable = true)
    String resume;

    @Column(nullable = true)
    String[] keywords;

    @Column(nullable = false)
    Timestamp creationDate;

    @Column(nullable = false)
    Timestamp signatureDate;

    @Column(nullable = false)
    Timestamp lastUpdateDate;

    @Column(nullable = false)
    boolean isActive;
}
