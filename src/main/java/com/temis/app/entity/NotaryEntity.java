package com.temis.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "notary")
public class NotaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(unique = true, nullable = false)
    Long Id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String responsible;

    @Column(nullable = false)
    int number;

    @Column(nullable = false)
    String address;

    @Column(nullable = false)
    String city;

    @Column(nullable = false)
    String state;

    @Column(nullable = false)
    String country;

    @Column(nullable = false)
    String zipCode;

    @Column(nullable = false)
    String phoneNumber;

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    Timestamp creationDate;

    @Column(nullable = false)
    Timestamp lastUpdateDate;

    @Column(nullable = false)
    boolean isActive;
}
