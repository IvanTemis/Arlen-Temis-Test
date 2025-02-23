package com.temis.app.entity;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FileData;
import com.google.cloud.vertexai.api.Part;
import com.temis.app.model.MessageSource;
import com.temis.app.model.VertexAiRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String nickName;
    
    @Enumerated(STRING)
    @Column(nullable = false)
    private MessageSource messageSource;

    @OrderBy("createdDate")
    @OneToMany(mappedBy = "context", cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    private List<MessageContextContentEntity> messageContents;

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

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Timestamp lastModifiedDate;

    @Setter
    @Column(nullable = false)
    boolean isActive = true;

    public List<String> getBodies(){
        return messageContents.stream().map(MessageContextContentEntity::getBody).toList();
    }
    public List<DocumentEntity> getDocumentEntities(){
        return messageContents.stream().map(MessageContextContentEntity::getDocumentEntity).filter(Objects::nonNull).toList();
    }

    public String getContentAsDebugString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (MessageContextContentEntity messageContent : messageContents) {
            stringBuilder.append(messageContent.getBody()).append('\n');

            if(messageContent.getDocumentEntity() != null){
                stringBuilder.append('[').append(messageContent.getDocumentEntity().getFileType());
                if(messageContent.getDocumentEntity().getDocumentType() != null){
                    stringBuilder.append(':').append(messageContent.getDocumentEntity().getDocumentType().getName());
                }
                stringBuilder.append("]\n");;
            }
        }

        return stringBuilder.toString();
    }

    public boolean isEmpty(){
        for (MessageContextContentEntity messageContent : messageContents) {
            if(!messageContent.isEmpty()){
                return false;
            }
        }
        return true;
    }

    public Content toVertexAiContent(){
        var aiContentBuilder = Content.newBuilder().setRole(VertexAiRole.USER.name());
        for (MessageContextContentEntity content : this.getMessageContents()) {
            if (content.isEmpty()) continue;

            String text = content.getBody();
            if (text == null || text.isEmpty()) {
                text = "documento";

                if (content.getDocumentEntity() != null && content.getDocumentEntity().getDocumentType() != null) {
                    text += " ";
                    text += content.getDocumentEntity().getDocumentType().getName();
                }

                text += ":";
            }
            text += "\n";
            aiContentBuilder.addParts(Part.newBuilder().setText(text));

            if (content.getDocumentEntity() != null) {
                aiContentBuilder.addParts(Part.newBuilder().setFileData(
                        FileData.newBuilder()
                                .setMimeType(content.getDocumentEntity().getFileType())
                                .setFileUri(content.getDocumentEntity().getPath())
                ));
            }
        }
        return aiContentBuilder.build();
    }
}
