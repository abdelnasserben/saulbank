package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cheques")
public class Cheque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chequeId;

    @ManyToOne
    @JoinColumn(name = "serial")
    private ChequeRequest serial;

    @ManyToOne
    @JoinColumn(name = "trunkId")
    private Trunk trunk;

    private String chequeNumber;

    private double amount;

    private String currency;

    private String failureReason;

    private String status;

    @ManyToOne
    @JoinColumn(name = "branchId")
    private Branch branch;

    @CurrentTimestamp(event = EventType.INSERT)
    private LocalDateTime createdAt;

    @CurrentTimestamp(event = EventType.UPDATE)
    private LocalDateTime updatedAt;
}