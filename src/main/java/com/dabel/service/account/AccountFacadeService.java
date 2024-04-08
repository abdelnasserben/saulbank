package com.dabel.service.account;

import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountFacadeService {

    private final AccountService accountService;
    private final AccountAffiliationService accountAffiliationService;
    private final AccountOperationService accountOperationService;

    public AccountFacadeService(AccountService accountService, AccountAffiliationService accountAffiliationService, AccountOperationService accountOperationService) {
        this.accountService = accountService;
        this.accountAffiliationService = accountAffiliationService;
        this.accountOperationService = accountOperationService;
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

    public List<AccountDto> findAllVault(BranchDto branchDto) {
        return accountService.findAllVault(branchDto);
    }

    public AccountDto findVault(BranchDto branchDto, String currency) {
        return accountService.findVault(branchDto, currency);
    }

    public List<LedgerDto> findAllLedgers(BranchDto branchDto) {
        return accountService.findAllLedgers(branchDto);
    }

    public LedgerDto findLedgerByBranchAndType(BranchDto branchDto, String ledgerType) {
        return accountService.findLedgerByBranchAndType(branchDto, ledgerType);
    }

    public List<TrunkDto> findAllTrunks() {
        return accountService.findAllTrunks();
    }

    public TrunkDto findTrunkById(Long trunkId) {
        return accountService.findTrunkById(trunkId);
    }

    public TrunkDto findTrunkByNumber(String accountNumber) {
        return accountService.findTrunkByNumber(accountNumber);
    }

    public TrunkDto findTrunkByCustomerAndAccountNumber(CustomerDto customerDto, String accountNumber) {
        return accountService.findTrunkByCustomerAndAccountNumber(customerDto, accountNumber);
    }

    public List<TrunkDto> findAllTrunks(CustomerDto customerDto) {
        return accountService.findAllTrunks(customerDto);
    }

    public List<TrunkDto> findAllTrunks(AccountDto accountDto) {
        return accountService.findAllTrunks(accountDto);
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

    public void addAffiliate(CustomerDto customerDto, String accountNumber) {
        accountAffiliationService.add(customerDto, accountNumber);
    }

    public void removeAffiliate(CustomerDto customerDto, String accountNumber) {
        accountAffiliationService.remove(customerDto, accountNumber);
    }

    public void debit(AccountDto accountDto, double amount) {
        accountOperationService.debit(accountDto, amount);
    }

    public void credit(AccountDto accountDto, double amount) {
        accountOperationService.debit(accountDto, amount);
    }
}
