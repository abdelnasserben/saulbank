package com.dabel.dto;

import com.dabel.app.validation.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.CreditCardNumber;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CardDto extends BasicDto {

    private Long cardId;
    private AccountDto account;

    @CardType
    private String cardType;
    @CreditCardNumber
    private String cardNumber;
    @NotBlank
    private String cardName;
    private LocalDate expirationDate;
    @Size(min = 3, max = 4)
    private String cvc;
    private int cvcChecked;
    private String failureReason;
}
