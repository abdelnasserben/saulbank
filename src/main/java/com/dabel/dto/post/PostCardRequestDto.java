package com.dabel.dto.post;

import com.dabel.app.validation.CardType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class PostCardRequestDto {
    @NotBlank
    private String accountNumber;
    @NotBlank
    private String customerIdentityNumber;
    @CardType
    private String cardType;
}
