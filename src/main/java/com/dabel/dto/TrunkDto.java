package com.dabel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class TrunkDto {

    private Long trunkId;
    private AccountDto account;
    private CustomerDto customer;
    private String membership;
}
