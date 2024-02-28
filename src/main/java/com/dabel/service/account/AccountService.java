package com.dabel.service.account;

import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.LedgerDto;
import com.dabel.dto.TrunkDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.AccountMapper;
import com.dabel.mapper.BranchMapper;
import com.dabel.mapper.LedgerMapper;
import com.dabel.mapper.TrunkMapper;
import com.dabel.model.Account;
import com.dabel.repository.AccountRepository;
import com.dabel.repository.LedgerRepository;
import com.dabel.repository.TrunkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TrunkRepository trunkRepository;
    private final LedgerRepository ledgerRepository;

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

    public AccountDto findByNumber(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return AccountMapper.toDto(account);
    }

    public AccountDto findVault(BranchDto branchDto, String currency) {
        return AccountMapper.toDto(accountRepository.findByBranchAndCurrencyAndIsVault(BranchMapper.toModel(branchDto), currency, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found")));
    }

    public LedgerDto findLedgerByBranchAndType(BranchDto branchDto, String ledgerType) {
        return LedgerMapper.toDto(ledgerRepository.findByBranchAndLedgerType(BranchMapper.toModel(branchDto), ledgerType)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found")));
    }

    public TrunkDto findTrunkByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return TrunkMapper.toDto(trunkRepository.findByAccount(account)
                .orElseThrow(() -> new ResourceNotFoundException("Customer account not found")));
    }

    public List<AccountDto> findAll() {
        return accountRepository.findAll().stream()
                .map(AccountMapper::toDto)
                .toList();
    }
}
