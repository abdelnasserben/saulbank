package com.dabel.dto;

import com.dabel.app.validation.AffiliationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class AccountMemberDto {

    @AffiliationType
    private String affiliationType;
    @NotBlank
    private String accountNumber;
    @Valid
    private CustomerDto customerDto;
}
