package com.temis.app.entity;

import com.temis.app.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder( buildMethodName = "hiddenBuild")
@Getter
@Entity
@Table(name = "message_response")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MessageResponseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;

    @OrderBy("createdDate")
    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    private List<MessageResponseContentEntity> responseContents;

    @Nullable
    @JoinColumn(nullable = true, updatable = false)
    @ManyToOne(optional = true, targetEntity = UserEntity.class)
    private UserEntity userEntity;

    @JoinColumn(nullable = false, updatable = false, name = "message_context_id")
    @ManyToOne(optional = false, targetEntity = MessageContextEntity.class)
    private MessageContextEntity messageContextEntity;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;

    @Nullable
    @Column(nullable = true, updatable = false, columnDefinition = "TEXT")
    private String exception;

    public String getContentAsDebugString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (var responseContent : responseContents) {
            stringBuilder.append(responseContent.getBody()).append('\n');

            if(responseContent.getMediaURL() != null){
                stringBuilder.append('[').append(responseContent.getMediaURL()).append("]\n");
            }
        }

        return stringBuilder.toString();
    }


    public static class MessageResponseEntityBuilder {
        public MessageResponseEntityBuilder addContent(String body, URI mediaURL, List<String> quickActions) {
            if(responseContents == null){
                this.responseContents = new ArrayList<>();
            }

            this.responseContents.add(MessageResponseContentEntity.builder()
                    .body(body)
                    .mediaURL(mediaURL)
                    .quickActions(quickActions)
                    .build());

            return this;
        }
        public MessageResponseEntityBuilder addContent(String body, List<String> quickActions){
            return addContent(body, null, quickActions);
        }

        public MessageResponseEntityBuilder addContent(String body, URI mediaURL){
            return addContent(body, mediaURL, null);
        }

        public MessageResponseEntityBuilder addContent(String body){
            return addContent(body, null, null);
        }

        public MessageResponseEntity build(){
            var result = this.hiddenBuild();

            for (MessageResponseContentEntity responseContent : result.responseContents) {
                responseContent.setResponse(result);
            }

            return result;
        }
    }
}
