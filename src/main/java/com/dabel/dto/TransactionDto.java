package com.dabel.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TransactionDto extends BasicDto {

    private Long transactionId;
    private String transactionType;
    private AccountDto initiatorAccount;
    private AccountDto receiverAccount;
    private double amount;
    private String currency;
    private String sourceType;
    private String sourceValue;
    private CustomerDto customer;
    private String reason;
    private String failureReason;
}
