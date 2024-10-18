package com.dabel.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoanRequestDto extends BasicDto {

    private Long requestId;
    private String loanType;
    private AccountDto associatedAccount;
    private CustomerDto borrower;
    private String currency;

    @Positive
    private double requestedAmount;

    @PositiveOrZero
    private double interestRate;
    @Positive
    private int duration;
    @PositiveOrZero
    private double applicationFees;
    private String reason;
    private String failureReason;
}
