package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "document_type")
public class DocumentTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;
}