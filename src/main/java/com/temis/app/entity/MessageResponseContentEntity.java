package com.temis.app.entity;

import com.temis.app.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@Entity
@Table(name = "message_response_content")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MessageResponseContentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = true, columnDefinition = "TEXT")
    private List<String> quickActions;

    @Column(nullable = true, columnDefinition = "TEXT")
    private URI mediaURL;

    @Setter
    @JoinColumn(nullable = false, updatable = false, name = "response_id")
    @ManyToOne(optional = false, targetEntity = MessageResponseEntity.class)
    private MessageResponseEntity response;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;
}
