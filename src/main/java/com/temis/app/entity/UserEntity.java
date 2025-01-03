package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@Table(name = "temis_user")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String nickName;

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    @Column(nullable = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = true)
    private Date lastInteractionDate;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Date createdDate;

    @Column(nullable = false, updatable = false)
    @LastModifiedDate
    private Date lastModifiedDate;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = true)
    private String passwordHash;

    @Nullable
    @OneToOne(mappedBy="userEntity", targetEntity = NotaryEmployeeEntity.class, optional = true)
    private NotaryEmployeeEntity employee;



    public String getSuitableName(){
        String name = nickName;

        if (firstName != null) {
            name = firstName;

            if (lastName != null) {
                name += " " + lastName;
            }
        }

        return name;
    }
}
