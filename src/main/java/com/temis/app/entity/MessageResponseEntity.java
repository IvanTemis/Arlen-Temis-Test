package com.temis.app.entity;

import com.temis.app.converter.JsonConverter;
import com.temis.app.model.MessageSource;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Map;

import static jakarta.persistence.EnumType.STRING;

@Builder
@Getter
@Entity
@Table
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

    @Nullable
    @JoinColumn(nullable = true, updatable = false)
    @ManyToOne(optional = true, targetEntity = UserEntity.class)
    UserEntity userEntity;

    @JoinColumn(nullable = false, updatable = false)
    @ManyToOne(optional = false, targetEntity = MessageContextEntity.class)
    MessageContextEntity messageContextEntity;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;
}
