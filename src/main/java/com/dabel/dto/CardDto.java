package com.dabel.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CardDto extends BasicDto {

    private Long cardId;

    private AccountDto account;

    private String cardType;

    private String cardNumber;

    private String cardName;

    private LocalDate expirationDate;

    private String cvc;

    private int cvcChecked;

    private String failureReason;
}
