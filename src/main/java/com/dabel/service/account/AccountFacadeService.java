package com.dabel.service.account;

import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.TrunkDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service facade for managing account and trunk operations, including saving, retrieving,
 * activating, and deactivating accounts and trunks, as well as handling account affiliations.
 */
@Service
public class AccountFacadeService {

    private final AccountService accountService;
    private final AccountAffiliationService accountAffiliationService;

    public AccountFacadeService(AccountAffiliationService accountAffiliationService) {
        this.accountAffiliationService = accountAffiliationService;
        this.accountService = this.accountAffiliationService.getAccountService();
    }

    public void saveAccount(AccountDto accountDto) {
        accountService.saveAccount(accountDto);
    }

    public TrunkDto saveTrunk(TrunkDto trunkDto) {
        return accountService.saveTrunk(trunkDto);
    }

    public List<AccountDto> getAllAccounts() {
        return accountService.findAllAccounts();
    }

    public AccountDto getAccountByNumber(String accountNumber) {
        return accountService.findAccountByNumber(accountNumber);
    }

    public List<TrunkDto> getAllTrunks() {
        return accountService.findAllTrunks();
    }

    public TrunkDto getTrunkById(Long trunkId) {
        return accountService.findTrunkById(trunkId);
    }

    public TrunkDto getTrunkByNumber(String accountNumber) {
        return accountService.findTrunkByAccountNumber(accountNumber);
    }

    public TrunkDto getTrunkByCustomerAndNumber(CustomerDto customerDto, String accountNumber) {
        return accountService.findTrunkByCustomerAndAccountNumber(customerDto, accountNumber);
    }

    public List<TrunkDto> getAllTrunksByCustomer(CustomerDto customerDto) {
        return accountService.findAllTrunksByCustomer(customerDto);
    }

    public List<TrunkDto> getAllTrunksByAccount(AccountDto accountDto) {
        return accountService.findAllTrunksByAccount(accountDto);
    }

    public void activateTrunkById(Long trunkId) {
        AccountDto accountDto = getTrunkById(trunkId).getAccount();
        if(!Helper.isActiveStatedObject(accountDto)) {
            accountDto.setStatus(Status.ACTIVE.code());
            accountDto.setUpdatedBy(Helper.getAuthenticated().getName());
            accountService.saveAccount(accountDto);
        }
    }

    public void deactivateTrunkById(Long trunkId) {
        AccountDto accountDto = getTrunkById(trunkId).getAccount();
        if(!Helper.isActiveStatedObject(accountDto))
            return;

        accountDto.setStatus(Status.INACTIVE.code());
        accountDto.setUpdatedBy(Helper.getAuthenticated().getName());
        accountService.saveAccount(accountDto);
    }

    public void addAffiliateToAccount(CustomerDto customerDto, String accountNumber) {
        accountAffiliationService.affiliate(customerDto, accountNumber);
    }

    public void removeAffiliateFromAccount(CustomerDto customerDto, String accountNumber) {
        accountAffiliationService.disaffiliate(customerDto, accountNumber);
    }

    public void debitAccount(AccountDto accountDto, double amount) {
        accountService.debitAccount(accountDto, amount);
    }

    public void creditAccount(AccountDto accountDto, double amount) {
        accountService.creditAccount(accountDto, amount);
    }
}
