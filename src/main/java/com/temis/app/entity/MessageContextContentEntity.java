package com.temis.app.entity;

import com.temis.app.converter.JsonConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "message_context_content")
@EntityListeners(AuditingEntityListener.class)
public class MessageContextContentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String messageId;

    @Column(nullable = false, columnDefinition = "text")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> request;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Nullable
    @Column(nullable = true, columnDefinition = "TEXT")
    private String mediaUrl;

    @Nullable
    @Column(nullable = true)
    private String mediaContentType;

    @Setter
    @Nullable
    @JoinColumn(nullable = true, name = "document_id")
    @ManyToOne(optional = true, targetEntity = DocumentEntity.class)
    private DocumentEntity documentEntity;

    @ManyToOne
    @JoinColumn(name = "context_id", nullable = false)
    private MessageContextEntity context;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;
}
