package com.dabel.service.branch;

import com.dabel.app.Helper;
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

    public void create(BranchDto branchDto, double[] vaultsAsset) {

        if (branchDto.getBranchId() == null) {

            branchDto.setStatus(Status.ACTIVE.code());
            BranchDto savedBranch = branchService.save(branchDto);
            createVaults(savedBranch, vaultsAsset);
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
                .accountName(String.format("GL Withdraw Fees Branch %d", savedBranch.getBranchId()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        AccountDto forTransferLedger = accountService.save(AccountDto.builder()
                .accountName(String.format("GL Transfer Fees Branch %d", savedBranch.getBranchId()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        AccountDto forLoanLedger = accountService.save(AccountDto.builder()
                .accountName(String.format("GL Loan Fees Branch %d", savedBranch.getBranchId()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        AccountDto forCardApplicationLedger = accountService.save(AccountDto.builder()
                .accountName(String.format("GL Card Application Fees Branch %d", savedBranch.getBranchId()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        AccountDto forChequeApplicationLedger = accountService.save(AccountDto.builder()
                .accountName(String.format("GL Cheque Application Fees Branch %d", savedBranch.getBranchId()))
                .accountNumber(Helper.generateAccountNumber())
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

    private void createVaults(BranchDto savedBranch, double[] vaultsAsset) {

        //TODO: build and save vault kmf
        accountService.save(AccountDto.builder()
                .accountName(String.format("Vault KMF %d", savedBranch.getBranchId()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.KMF.name())
                .balance(vaultsAsset[0])
                .isVault(1)
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        //TODO: build and save vault eur
        accountService.save(AccountDto.builder()
                .accountName(String.format("Vault EUR %d", savedBranch.getBranchId()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.EUR.name())
                .balance(vaultsAsset[1])
                .isVault(1)
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());

        //TODO: build and save vault usd
        accountService.save(AccountDto.builder()
                .accountName(String.format("Vault USD %d", savedBranch.getBranchId()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .currency(Currency.USD.name())
                .balance(vaultsAsset[2])
                .isVault(1)
                .branch(savedBranch)
                .status(savedBranch.getStatus())
                .build());
    }

    public List<AccountDto> findAllVaultsByBranchId(Long branchId) {
        BranchDto branchDto = branchService.findById(branchId);
        return accountService.findAllVault(branchDto);
    }

    public List<LedgerDto> findAllLedgersByBranchId(Long branchId) {
        BranchDto branchDto = branchService.findById(branchId);
        return accountService.findAllLedgers(branchDto);
    }

    public AccountDto findVaultByBranchIdAndCurrency(Long branchId, String currency) {
        BranchDto branchDto = branchService.findById(branchId);
        return accountService.findVault(branchDto, currency);
    }
}
