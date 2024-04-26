package com.dabel.service.account;

import com.dabel.app.Helper;
import com.dabel.app.JpaHelper;
import com.dabel.dto.*;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.*;
import com.dabel.model.Account;
import com.dabel.model.Trunk;
import com.dabel.repository.AccountRepository;
import com.dabel.repository.LedgerRepository;
import com.dabel.repository.TrunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TrunkRepository trunkRepository;
    private final LedgerRepository ledgerRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, TrunkRepository trunkRepository, LedgerRepository ledgerRepository) {
        this.accountRepository = accountRepository;
        this.trunkRepository = trunkRepository;
        this.ledgerRepository = ledgerRepository;
    }

    public AccountDto save(AccountDto accountDTO) {

        Account account = accountRepository.save(AccountMapper.toModel(accountDTO));
        return AccountMapper.toDto(account);
    }

    public TrunkDto save(TrunkDto trunkDto) {

        Account savedAccount = accountRepository.save(AccountMapper.toModel(trunkDto.getAccount()));
        trunkDto.setAccount(AccountMapper.toDto(savedAccount));
        return TrunkMapper.toDto(trunkRepository.save(TrunkMapper.toModel(trunkDto)));
    }

    public LedgerDto save(LedgerDto ledgerDto) {

        Account savedAccount = accountRepository.save(AccountMapper.toModel(ledgerDto.getAccount()));
        ledgerDto.setAccount(AccountMapper.toDto(savedAccount));
        ledgerDto.setBranch(ledgerDto.getAccount().getBranch());
        return LedgerMapper.toDto(ledgerRepository.save(LedgerMapper.toModel(ledgerDto)));
    }

    public List<AccountDto> findAll() {
        return accountRepository.findAll().stream()
                .map(AccountMapper::toDto)
                .toList();
    }

    public AccountDto findByNumber(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return AccountMapper.toDto(account);
    }

    public List<AccountDto> findAllVaults(BranchDto branchDto) {
        return accountRepository.findAllByBranchAndIsVault(BranchMapper.toModel(branchDto), 1).stream()
                .map(AccountMapper::toDto)
                .toList();
    }

    public AccountDto findVault(BranchDto branchDto, String currency) {
        return AccountMapper.toDto(accountRepository.findByBranchAndCurrencyAndIsVault(BranchMapper.toModel(branchDto), currency, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found")));
    }

    public List<LedgerDto> findAllLedgers(BranchDto branchDto) {
        return ledgerRepository.findAllByBranch(BranchMapper.toModel(branchDto)).stream()
                .map(LedgerMapper::toDto)
                .toList();
    }

    public LedgerDto findLedgerByBranchAndType(BranchDto branchDto, String ledgerType) {
        return LedgerMapper.toDto(ledgerRepository.findByBranchAndLedgerType(BranchMapper.toModel(branchDto), ledgerType)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found")));
    }

    public List<TrunkDto> findAllTrunks() {
        return trunkRepository.findAll().stream()
                .filter(JpaHelper.distinctByKey(Trunk::getAccount))
                .map(TrunkMapper::toDto)
                .toList();
    }

    public List<TrunkDto> findAllTrunks(CustomerDto customerDto) {
        return trunkRepository.findAllByCustomer(CustomerMapper.toModel(customerDto)).stream()
                .map(TrunkMapper::toDto)
                .toList();
    }

    public List<TrunkDto> findAllTrunks(AccountDto accountDto) {
        return trunkRepository.findAllByAccount(AccountMapper.toModel(accountDto)).stream()
                .map(TrunkMapper::toDto)
                .toList();
    }

    public TrunkDto findTrunk(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Trunk trunk = trunkRepository.findAllByAccount(account).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Customer account not found"));

        return TrunkMapper.toDto(trunk);
    }

    public TrunkDto findTrunk(Long trunkId) {
        return TrunkMapper.toDto(trunkRepository.findById(trunkId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found")));
    }

    public TrunkDto findTrunk(CustomerDto customerDto, String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return TrunkMapper.toDto(trunkRepository.findByCustomerAndAccount(CustomerMapper.toModel(customerDto), account)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found")));
    }

    public void deleteTrunk(TrunkDto trunkDto) {
        trunkRepository.delete(TrunkMapper.toModel(trunkDto));
    }

    public void debit(AccountDto accountDto, double amount) {
        accountDto.setBalance(accountDto.getBalance() - Helper.formatAmount(amount));
        accountRepository.save(AccountMapper.toModel(accountDto));
    }

    public void credit(AccountDto accountDto, double amount) {
        accountDto.setBalance(accountDto.getBalance() + Helper.formatAmount(amount));
        accountRepository.save(AccountMapper.toModel(accountDto));
    }
}
