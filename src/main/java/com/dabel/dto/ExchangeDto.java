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
public class ExchangeDto extends BasicDto {

    private Long exchangeId;
    @NotBlank
    private String customerIdentityNumber;
    @NotBlank
    private String customerFullName;
    @Currency
    private String purchaseCurrency;
    @Positive
    private double purchaseAmount;
    @Currency
    private String saleCurrency;
    private double saleAmount;
    private String reason;
    private String failureReason;
}
