package com.dabel.service.account;

import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.LedgerDto;
import com.dabel.dto.TrunkDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountFacadeService {

    private final AccountService accountService;
    private final AccountAffiliationService accountAffiliationService;

    @Autowired
    public AccountFacadeService(AccountAffiliationService accountAffiliationService) {
        this.accountAffiliationService = accountAffiliationService;
        this.accountService = this.accountAffiliationService.getAccountService();
    }

    public AccountDto save(AccountDto accountDto) {
        return accountService.save(accountDto);
    }

    public TrunkDto save(TrunkDto trunkDto) {
        return accountService.save(trunkDto);
    }

    public LedgerDto save(LedgerDto ledgerDto) {
        return accountService.save(ledgerDto);
    }

    public List<AccountDto> findAll() {
        return accountService.findAll();
    }

    public AccountDto findByNumber(String accountNumber) {
        return accountService.findByNumber(accountNumber);
    }

    public List<TrunkDto> findAllTrunks() {
        return accountService.findAllTrunks();
    }

    public TrunkDto findTrunkById(Long trunkId) {
        return accountService.findTrunk(trunkId);
    }

    public TrunkDto findTrunkByNumber(String accountNumber) {
        return accountService.findTrunk(accountNumber);
    }

    public TrunkDto findTrunkByCustomerAndAccountNumber(CustomerDto customerDto, String accountNumber) {
        return accountService.findTrunk(customerDto, accountNumber);
    }

    public List<TrunkDto> findAllTrunks(CustomerDto customerDto) {
        return accountService.findAllTrunks(customerDto);
    }

    public List<TrunkDto> findAllTrunks(AccountDto accountDto) {
        return accountService.findAllTrunks(accountDto);
    }

    public void activateTrunk(Long trunkId) {
        AccountDto accountDto = findTrunkById(trunkId).getAccount();
        if(!Helper.isActiveStatedObject(accountDto)) {
            accountDto.setStatus(Status.ACTIVE.code());
            accountDto.setUpdatedBy(Helper.getAuthenticated().getName());
            accountService.save(accountDto);
        }
    }

    public void deactivateTrunk(Long trunkId) {
        AccountDto accountDto = findTrunkById(trunkId).getAccount();
        if(!Helper.isActiveStatedObject(accountDto))
            return;

        accountDto.setStatus(Status.DEACTIVATED.code());
        accountDto.setUpdatedBy(Helper.getAuthenticated().getName());
        accountService.save(accountDto);
    }

    public void addAffiliate(CustomerDto customerDto, String accountNumber) {
        accountAffiliationService.affiliate(customerDto, accountNumber);
    }

    public void removeAffiliate(CustomerDto customerDto, String accountNumber) {
        accountAffiliationService.disaffiliate(customerDto, accountNumber);
    }

    public void debit(AccountDto accountDto, double amount) {
        accountService.debit(accountDto, amount);
    }

    public void credit(AccountDto accountDto, double amount) {
        accountService.credit(accountDto, amount);
    }
}
