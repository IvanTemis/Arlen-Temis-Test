package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "personal_document_type")
public class PersonalDocumentTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(unique = true, nullable = false)
    Long Id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String description;
}
