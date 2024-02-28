package com.dabel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class LedgerDto {

    private Long ledgerId;
    private BranchDto branch;
    private AccountDto account;
    private String ledgerType;
}
