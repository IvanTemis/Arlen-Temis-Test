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
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "message_context")
@EntityListeners(AuditingEntityListener.class)
public class MessageContextEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String messageId;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String mediaUrl;

    @Nullable
    @Column
    private String mediaContentType;

    @Enumerated(STRING)
    @Column(nullable = false)
    private MessageSource messageSource;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private Map<String, Object> request;

    @ManyToOne(optional = true)
    @JoinColumn(name = "document_id")
    private DocumentEntity documentEntity;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToOne(optional = true)
    @JoinColumn(name = "service_id")
    private ServiceEntity serviceEntity;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdDate;
}