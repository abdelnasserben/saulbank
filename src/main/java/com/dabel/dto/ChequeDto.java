package com.dabel.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChequeDto extends BasicDto {

    private Long chequeId;

    private TrunkDto trunk;

    private String chequeNumber;

    private String currency;

    private double amount;

    private String failureReason;
}
