package com.dabel.dto;

import com.dabel.app.validation.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Range;

@Data
@SuperBuilder
@NoArgsConstructor
public class PostCardDto {

    private String accountNumber;

    @CardType
    private String cardType;

    @CreditCardNumber
    private String cardNumber;

    @NotBlank
    private String cardName;

    @Range(min = 1, max = 12)
    private String expiryMonth;

    @Size(min = 4, max = 4, message = "Year must be 4 digits")
    private String expiryYear;

    @Size(min = 3, max = 4)
    private String cvc;

}
