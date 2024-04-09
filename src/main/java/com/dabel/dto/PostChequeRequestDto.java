package com.dabel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class PostChequeRequestDto {
    @NotBlank
    private String accountNumber;
    @NotBlank
    private String customerIdentityNumber;
}
