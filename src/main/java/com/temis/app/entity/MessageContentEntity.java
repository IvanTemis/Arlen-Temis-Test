package com.temis.app.entity;

import com.temis.app.converter.JsonConverter;
import com.temis.app.model.MessageSource;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;

import static jakarta.persistence.EnumType.STRING;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "message_content")
@EntityListeners(AuditingEntityListener.class)
public class MessageContentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String messageId;

    @Column(nullable = false, columnDefinition = "text")
    @Convert(converter = JsonConverter.class)
    Map<String, Object> request;

    @Column(nullable = false, columnDefinition = "TEXT")
    String body;

    @Nullable
    @Column(nullable = true, columnDefinition = "TEXT")
    String mediaUrl;

    @Nullable
    @Column(nullable = true)
    String mediaContentType;

    @Setter
    @Nullable
    @JoinColumn(nullable = true, name = "document_id")
    @ManyToOne(optional = true, targetEntity = DocumentEntity.class)
    DocumentEntity documentEntity;

    @ManyToOne
    @JoinColumn(name = "context_id", nullable = false)
    MessageContextEntity context;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;
}
