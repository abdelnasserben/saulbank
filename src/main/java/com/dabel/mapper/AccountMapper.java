package com.dabel.mapper;

import com.dabel.dto.AccountDto;
import com.dabel.model.Account;
import org.modelmapper.ModelMapper;

public class AccountMapper {

    private static final ModelMapper mapper = new ModelMapper();

    public static Account toModel(AccountDto accountDto) {
        return mapper.map(accountDto, Account.class);
    }

    public static AccountDto toDto(Account account) {
        return mapper.map(account, AccountDto.class);
    }

}
