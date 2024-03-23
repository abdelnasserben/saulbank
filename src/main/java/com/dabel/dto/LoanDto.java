package com.dabel.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoanDto extends BasicDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;
    private String loanType;
    private AccountDto account;
    private CustomerDto borrower;
    private String currency;
    private double issuedAmount;
    private double interestRate;
    private int duration;
    private double applicationFees;
    private double totalAmount;
    private String reason;
    private String failureReason;
}
