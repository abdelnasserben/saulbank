package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;
    private String loanType;

    @ManyToOne
    @JoinColumn(name = "accountId")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "borrowerId")
    private Customer borrower;

    @ManyToOne
    @JoinColumn(name = "associatedAccountId")
    private Account associatedAccount;

    private String currency;
    private double issuedAmount;
    private double interestRate;
    private int duration;
    private double applicationFees;
    private double totalAmount;
    private String reason;
    private String failureReason;
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
