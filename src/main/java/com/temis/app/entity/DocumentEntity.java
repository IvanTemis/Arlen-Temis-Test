package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

@Data
@Entity
@Table(name = "document")
public class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Nullable
    @JoinColumn(name = "document_type_id", nullable = true)
    @ManyToOne(optional = true, targetEntity = DocumentTypeEntity.class)
    private DocumentTypeEntity documentType;

    @Setter
    @Nullable
    @JoinColumn(nullable = true, name = "uploaded_by_id")
    @ManyToOne(optional = true, targetEntity = UserEntity.class)
    UserEntity uploadedBy;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = true)
    private Timestamp issueDate;

    @Column(nullable = true)
    private Timestamp expirationDate;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private String path;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date receptionDate;

    @Column(nullable = false)
    @LastModifiedDate
    private Date lastModifiedDate;

    @ManyToMany(mappedBy = "generationInput")
    Set<NotaryDocumentEntity> generationOutput;
}