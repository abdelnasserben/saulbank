package com.dabel.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CardRequestDto extends BasicDto {

    private Long requestId;
    private TrunkDto trunk;
    private String cardType;
    private String failureReason;
}
