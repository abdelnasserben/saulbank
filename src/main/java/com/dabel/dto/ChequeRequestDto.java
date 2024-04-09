package com.dabel.dto;

import com.dabel.model.ChequeRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChequeRequestDto extends BasicDto {

    private Long requestId;
    private ChequeRequest serial;
    private TrunkDto trunk;
    private String failureReason;
    private String status;
}
