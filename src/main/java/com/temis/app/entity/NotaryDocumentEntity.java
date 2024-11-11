package com.temis.app.entity;

import com.temis.app.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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

    @JoinColumn(nullable = false, name = "document_id")
    @OneToOne(optional = false, targetEntity = DocumentEntity.class)
    DocumentEntity documentEntity;

    @JoinColumn(nullable = false, name = "service_id")
    @ManyToOne(optional = false, targetEntity = ServiceEntity.class)
    ServiceEntity serviceEntity;

    @Column(columnDefinition = "TEXT")
    private String resume;

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp signatureDate;

    @Convert(converter = StringListConverter.class)
    @Column(name = "keywords")
    private Set<String> keywords;

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

    @JoinTable(name = "document_generation_input", joinColumns = @JoinColumn(name = "notary_document_id"), inverseJoinColumns = @JoinColumn(name = "document_id"))
    @ManyToMany(targetEntity = DocumentEntity.class)
    Set<DocumentEntity> generationInput;
}