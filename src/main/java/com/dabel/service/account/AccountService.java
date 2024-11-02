package com.dabel.service.account;

import com.dabel.app.Helper;
import com.dabel.dto.*;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.*;
import com.dabel.model.Account;
import com.dabel.model.Trunk;
import com.dabel.repository.AccountRepository;
import com.dabel.repository.LedgerRepository;
import com.dabel.repository.TrunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Service
public class AccountService {

    private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found";

    private final AccountRepository accountRepository;
    private final TrunkRepository trunkRepository;
    private final LedgerRepository ledgerRepository;
    private final String currentUsername = Helper.getAuthenticated().getName();

    public AccountService(AccountRepository accountRepository, TrunkRepository trunkRepository, LedgerRepository ledgerRepository) {
        this.accountRepository = accountRepository;
        this.trunkRepository = trunkRepository;
        this.ledgerRepository = ledgerRepository;
    }

    public AccountDto saveAccount(AccountDto accountDTO) {

        Account account = accountRepository.save(AccountMapper.toEntity(accountDTO));
        return AccountMapper.toDto(account);
    }

    public TrunkDto saveTrunk(TrunkDto trunkDto) {

        Account savedAccount = accountRepository.save(AccountMapper.toEntity(trunkDto.getAccount()));
        trunkDto.setAccount(AccountMapper.toDto(savedAccount));
        return TrunkMapper.toDto(trunkRepository.save(TrunkMapper.toEntity(trunkDto)));
    }

    public LedgerDto saveLedger(LedgerDto ledgerDto) {

        Account savedAccount = accountRepository.save(AccountMapper.toEntity(ledgerDto.getAccount()));
        ledgerDto.setAccount(AccountMapper.toDto(savedAccount));
        ledgerDto.setBranch(ledgerDto.getAccount().getBranch());
        return LedgerMapper.toDto(ledgerRepository.save(LedgerMapper.toEntity(ledgerDto)));
    }

    public List<AccountDto> findAllAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountMapper::toDto)
                .toList();
    }

    public AccountDto findAccountByNumber(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
        return AccountMapper.toDto(account);
    }

    public List<AccountDto> findAllVaultsByBranch(BranchDto branchDto) {
        return accountRepository.findAllByBranchAndIsVault(BranchMapper.toEntity(branchDto), 1).stream()
                .map(AccountMapper::toDto)
                .toList();
    }

    public AccountDto findVaultByBranchAndCurrency(BranchDto branchDto, String currency) {
        return AccountMapper.toDto(accountRepository.findByBranchAndCurrencyAndIsVault(BranchMapper.toEntity(branchDto), currency, 1)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE)));
    }

    public List<LedgerDto> findAllLedgersByBranch(BranchDto branchDto) {
        return ledgerRepository.findAllByBranch(BranchMapper.toEntity(branchDto)).stream()
                .map(LedgerMapper::toDto)
                .toList();
    }

    public LedgerDto findLedgerByBranchAndType(BranchDto branchDto, String ledgerType) {
        return LedgerMapper.toDto(ledgerRepository.findByBranchAndLedgerType(BranchMapper.toEntity(branchDto), ledgerType)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE)));
    }

    public List<TrunkDto> findAllTrunks() {
        return trunkRepository.findAll().stream()
                .filter(distinctByAccount())
                .map(TrunkMapper::toDto)
                .toList();
    }

    public List<TrunkDto> findAllTrunksByCustomer(CustomerDto customerDto) {
        return trunkRepository.findAllByCustomer(CustomerMapper.toEntity(customerDto)).stream()
                .map(TrunkMapper::toDto)
                .toList();
    }

    public List<TrunkDto> findAllTrunksByAccount(AccountDto accountDto) {
        return trunkRepository.findAllByAccount(AccountMapper.toEntity(accountDto)).stream()
                .map(TrunkMapper::toDto)
                .toList();
    }

    public TrunkDto findTrunkByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));

        Trunk trunk = trunkRepository.findAllByAccount(account).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Customer account not found"));

        return TrunkMapper.toDto(trunk);
    }

    public TrunkDto findTrunkById(Long trunkId) {
        return TrunkMapper.toDto(trunkRepository.findById(trunkId)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE)));
    }

    public TrunkDto findTrunkByCustomerAndAccountNumber(CustomerDto customerDto, String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
        return TrunkMapper.toDto(trunkRepository.findByCustomerAndAccount(CustomerMapper.toEntity(customerDto), account)
                .orElseThrow(() -> new ResourceNotFoundException(ACCOUNT_NOT_FOUND_MESSAGE)));
    }

    public void deleteTrunk(TrunkDto trunkDto) {
        trunkRepository.delete(TrunkMapper.toEntity(trunkDto));
    }

    public void debitAccount(AccountDto accountDto, double amount) {
        updateAccountBalance(accountDto, -amount);
    }

    public void creditAccount(AccountDto accountDto, double amount) {
        updateAccountBalance(accountDto, amount);
    }

    private void updateAccountBalance(AccountDto accountDto, double amount) {
        double updatedBalance = accountDto.getBalance() + Helper.formatAmount(amount);
        accountDto.setBalance(updatedBalance);
        accountDto.setUpdatedBy(Helper.getAuthenticated().getName());
        accountRepository.save(AccountMapper.toEntity(accountDto));
    }

    private Predicate<Trunk> distinctByAccount() {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return trunk -> seen.add(trunk.getAccount());
    }

}
