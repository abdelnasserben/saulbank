package com.dabel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class PostChequeDto {

    @NotBlank
    private String chequeNumber;

    @NotBlank
    private String beneficiaryAccountNumber;

    @Positive
    private double amount;

    private String reason;

}
