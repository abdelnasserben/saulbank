package com.dabel.service.account;

import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountFacadeService {

    private final AccountService accountService;

    public AccountFacadeService(AccountService accountService) {
        this.accountService = accountService;
    }

    public List<TrunkDto> findAllCustomerAccounts(CustomerDto customerDto) {
        return accountService.findAllTrunks(customerDto);
    }
}
