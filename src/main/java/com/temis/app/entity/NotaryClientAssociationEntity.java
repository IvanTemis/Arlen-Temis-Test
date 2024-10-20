package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "notary_client_association")
public class NotaryClientAssociationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "notary_id", nullable = true)
    private NotaryEntity notary;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    private NotaryClientEntity client;

    @Column(nullable = false)
    private Timestamp creationDate;
}