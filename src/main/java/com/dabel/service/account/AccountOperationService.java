package com.dabel.service.account;

import com.dabel.app.Helper;
import com.dabel.dto.AccountDto;
import org.springframework.stereotype.Service;

@Service
public class AccountOperationService {

    private final AccountService accountService;

    public AccountOperationService(AccountService accountService) {
        this.accountService = accountService;
    }

    public void debit(AccountDto account, double amount) {
        account.setBalance(account.getBalance() - Helper.formatAmount(amount));
        accountService.save(account);
    }

    public void credit(AccountDto account, double amount) {
        account.setBalance(account.getBalance() + Helper.formatAmount(amount));
        accountService.save(account);
    }
}