package com.temis.app.entity;

import com.google.cloud.vertexai.api.Content;
import com.google.protobuf.AbstractMessageLite;
import com.temis.app.model.VertexAiRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;

@Getter
@Entity
@Table(name = "ai_chat_content")
@EntityListeners(AuditingEntityListener.class)
public class VertexAiContentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    @Enumerated(STRING)
    VertexAiRole role;

    @ElementCollection(targetClass = byte[].class, fetch = FetchType.EAGER)
    @CollectionTable(name = "content_parts", joinColumns = @JoinColumn(name = "content_id"))
    @Column(nullable = false)
    List<byte[]> parts;

    @Setter
    @Nullable
    @JoinColumn(nullable = false, name = "user_id")
    @ManyToOne(optional = false, targetEntity = UserEntity.class)
    UserEntity userEntity;

    @Column(nullable = false, name = "agent_id")
    private String agentId;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;

    public static VertexAiContentEntity fromContent(UserEntity userEntity, Content content, String agentId){
        var result = new VertexAiContentEntity();
        result.userEntity = userEntity;
        result.agentId = agentId;
        result.role = VertexAiRole.valueOf(content.getRole().toUpperCase());
        result.parts = content.getPartsList().stream().map(AbstractMessageLite::toByteArray).toList();
        return result;
    }
}
