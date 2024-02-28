package com.dabel.service.branch;

import com.dabel.app.Generator;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.LedgerDto;
import com.dabel.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchFacadeService {

    @Autowired
    BranchService branchService;
    @Autowired
    AccountService accountService;

    public void create(BranchDto branchDto) {

        if (branchDto.getBranchId() == null) {

            branchDto.setStatus(Status.ACTIVE.code());
            BranchDto savedBranch = branchService.save(branchDto);
            createVaults(savedBranch);
            createGL(savedBranch);

        } else branchService.save(branchDto);
    }

    public BranchDto findById(Long branchId) {
        return branchService.findById(branchId);
    }

    public List<BranchDto> findAll() {
        return branchService.findAll();
    }

    private void createGL(BranchDto savedBranch) {
        AccountDto forWithdrawLedger = accountService.save(AccountDto.builder()
                .accountName(String.format("GL %d", savedBranch.getBranchId()))
                .accountNumber(Generator.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        AccountDto forTransferLedger = accountService.save(AccountDto.builder()
                .accountName(String.format("GL %d", savedBranch.getBranchId()))
                .accountNumber(Generator.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        AccountDto forLoanLedger = accountService.save(AccountDto.builder()
                .accountName(String.format("GL %d", savedBranch.getBranchId()))
                .accountNumber(Generator.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        AccountDto forCardApplicationLedger = accountService.save(AccountDto.builder()
                .accountName(String.format("GL %d", savedBranch.getBranchId()))
                .accountNumber(Generator.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        AccountDto forChequeApplicationLedger = accountService.save(AccountDto.builder()
                .accountName(String.format("GL %d", savedBranch.getBranchId()))
                .accountNumber(Generator.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        accountService.save(LedgerDto.builder()
                .ledgerType(LedgerType.WITHDRAW.name())
                .account(forWithdrawLedger)
                .build());

        accountService.save(LedgerDto.builder()
                .ledgerType(LedgerType.TRANSFER.name())
                .account(forTransferLedger)
                .build());

        accountService.save(LedgerDto.builder()
                .ledgerType(LedgerType.LOAN.name())
                .account(forLoanLedger)
                .build());

        accountService.save(LedgerDto.builder()
                .ledgerType(LedgerType.CARD_REQUEST.name())
                .account(forCardApplicationLedger)
                .build());

        accountService.save(LedgerDto.builder()
                .ledgerType(LedgerType.CHEQUE_REQUEST.name())
                .account(forChequeApplicationLedger)
                .build());
    }

    private void createVaults(BranchDto savedBranch) {
        //TODO: build and save vault eur
        accountService.save(AccountDto.builder()
                .accountName(String.format("Vault EUR %d", savedBranch.getBranchId()))
                .accountNumber(Generator.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.EUR.name())
                .isVault(1)
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        //TODO: build and save vault usd
        accountService.save(AccountDto.builder()
                .accountName(String.format("Vault USD %d", savedBranch.getBranchId()))
                .accountNumber(Generator.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.USD.name())
                .isVault(1)
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        //TODO: build and save vault kmf
        accountService.save(AccountDto.builder()
                .accountName(String.format("Vault KMF %d", savedBranch.getBranchId()))
                .accountNumber(Generator.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .isVault(1)
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());
    }
}
