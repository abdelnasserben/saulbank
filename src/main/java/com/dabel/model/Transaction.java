package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    private String transactionType;
    @ManyToOne
    @JoinColumn(name = "initiatorAccountId")
    private Account initiatorAccount;

    @ManyToOne
    @JoinColumn(name = "receiverAccountId")
    private Account receiverAccount;

    private double amount;

    private String currency;

    private String sourceType;

    private String sourceValue;

    private String customerIdentity;

    private String customerFullName;

    private String reason;

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
