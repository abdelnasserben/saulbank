package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.generator.EventType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;
    private String firstName;
    private String lastName;
    private String gender;
    private String identityNumber;
    private String identityType;
    private String identityIssue;
    private LocalDate identityExpiration;
    private LocalDate birthDate;
    private String birthPlace;
    private String nationality;
    private String residence;
    private String address;
    private String postCode;
    private String phone;
    private String email;
    private String profession;
    private String profilePicture;
    private String signaturePicture;
    private String identityPicture;
    private String status;

    @ManyToOne
    @JoinColumn(name = "branchId")
    private Branch branch;

    @CurrentTimestamp(event = EventType.INSERT)
    private LocalDateTime createdAt;

    @CurrentTimestamp(event = EventType.UPDATE)
    private LocalDateTime updatedAt;
}
