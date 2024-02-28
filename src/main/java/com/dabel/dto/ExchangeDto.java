package com.dabel.dto;

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
    private String customerFullName;
    private String customerIdentityNumber;
    private String purchaseCurrency;
    private double purchaseAmount;
    private String saleCurrency;
    private double saleAmount;
    private String reason;
    private String failureReason;
}
