package com.dabel.dto;

import com.dabel.app.validation.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
    @Positive
    private double amount;
    @Currency
    private String currency;

    private String sourceType;
    private String sourceValue;

    @NotBlank
    private String customerIdentity;

    @NotBlank
    private String customerFullName;
    private String reason;
    private String failureReason;
}
