package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loan_requests")
public class LoanRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;
    private String loanType;

    @ManyToOne
    @JoinColumn(name = "borrowerId")
    private Customer borrower;

    @ManyToOne
    @JoinColumn(name = "accountId")
    private Account associatedAccount;

    private String currency;
    private double requestedAmount;
    private double interestRate;
    private int duration;
    private double applicationFees;
    private String failureReason;
    private String reason;
    private String status;
    private String initiatedBy;
    private String updatedBy;

    @ManyToOne
    @JoinColumn(name = "branchId")
    private Branch branch;

    @CurrentTimestamp(event = EventType.INSERT)
    private LocalDateTime createdAt;

    @CurrentTimestamp(event = EventType.UPDATE)
    private LocalDateTime updatedAt;
}
