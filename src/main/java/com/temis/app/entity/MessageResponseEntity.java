package com.temis.app.entity;

import com.temis.app.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@Entity
@Table(name = "message_response")
@EntityListeners(AuditingEntityListener.class)
public class MessageResponseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String body;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = true)
    private List<String> quickActions;

    @Column(nullable = true)
    URI mediaURL;

    @Nullable
    @JoinColumn(nullable = true, updatable = false)
    @ManyToOne(optional = true, targetEntity = UserEntity.class)
    UserEntity userEntity;

    @JoinColumn(nullable = false, updatable = false, name = "message_context_id")
    @ManyToOne(optional = false, targetEntity = MessageContextEntity.class)
    MessageContextEntity messageContextEntity;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;
}
