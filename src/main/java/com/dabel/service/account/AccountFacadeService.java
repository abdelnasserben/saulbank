package com.dabel.service.account;

import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
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

    public List<TrunkDto> findCustomerTrunks() {
        return accountService.findAllTrunks();
    }

    public List<TrunkDto> findCustomerTrunks(CustomerDto customerDto) {
        return accountService.findAllTrunks(customerDto);
    }

    public TrunkDto findTrunkByNumber(String accountNumber) {
        return accountService.findTrunkByNumber(accountNumber);
    }

    public AccountDto findVault(BranchDto branchDto, String currency) {
        return accountService.findVault(branchDto, currency);
    }

    public TrunkDto findTrunkByCustomerAndAccountNumber(CustomerDto customerDto, String accountNumber) {
        return accountService.findTrunkByCustomerAndAccountNumber(customerDto, accountNumber);
    }

    public TrunkDto findTrunkById(Long trunkId) {
        return accountService.findTrunkById(trunkId);
    }

    public void activateTrunk(Long trunkId) {
        AccountDto accountDto = findTrunkById(trunkId).getAccount();
        if(Helper.isInactiveAccount(accountDto)) {
            accountDto.setStatus(Status.ACTIVE.code());
            accountService.save(accountDto);
        }
    }

    public void deactivateTrunk(Long trunkId) {
        AccountDto accountDto = findTrunkById(trunkId).getAccount();
        if(Helper.isInactiveAccount(accountDto))
            return;

        accountDto.setStatus(Status.DEACTIVATED.code());
        accountService.save(accountDto);
    }
}
