package com.dabel.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AccountDto extends BasicDto {

    private Long accountId;
    private String accountName;
    private String accountNumber;
    private String accountType;
    private String accountProfile;
    private double balance;
    private String currency;
    private int isVault;
}
