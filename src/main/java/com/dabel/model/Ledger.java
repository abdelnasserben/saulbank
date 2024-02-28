package com.dabel.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ledgers", uniqueConstraints = {@UniqueConstraint(columnNames = {"branchId", "ledgerType"})})
public class Ledger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ledgerId;

    @ManyToOne
    @JoinColumn(name = "branchId")
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "accountNumber")
    private Account account;

    private String ledgerType;
}
