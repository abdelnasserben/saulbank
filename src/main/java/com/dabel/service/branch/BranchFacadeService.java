package com.dabel.service.branch;

import com.dabel.app.Helper;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.LedgerDto;
import com.dabel.service.account.AccountService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service facade for managing branch operations, including creating and retrieving branches,
 * vaults, and ledgers, as well as initializing accounts and ledgers associated with a branch.
 */
@Service
public class BranchFacadeService {

    private final BranchService branchService;
    private final AccountService accountService;
    private String currentUsername;

    public BranchFacadeService(BranchService branchService, AccountService accountService) {
        this.branchService = branchService;
        this.accountService = accountService;
    }


    /**
     * Creates a new branch and its associated vaults and general ledger accounts if branch ID is null.
     *
     * @param branchDto   Data transfer object representing the branch.
     * @param vaultsAsset Array containing initial asset values for each currency vault.
     */
    public void create(BranchDto branchDto, double[] vaultsAsset) {

        if (branchDto.getBranchId() == null) {

            branchDto.setStatus(Status.ACTIVE.code());
            BranchDto savedBranch = branchService.save(branchDto);
            createVaults(savedBranch, vaultsAsset);
            createGL(savedBranch);

        } else branchService.save(branchDto);
    }

    public BranchDto getById(Long branchId) {
        return branchService.findById(branchId);
    }

    public List<BranchDto> getAll() {
        return branchService.findAll();
    }

    public List<AccountDto> getAllVaultsByBranchId(Long branchId) {
        BranchDto branchDto = branchService.findById(branchId);
        return accountService.findAllVaultsByBranch(branchDto);
    }

    public List<LedgerDto> getAllLedgersByBranchId(Long branchId) {
        BranchDto branchDto = branchService.findById(branchId);
        return accountService.findAllLedgersByBranch(branchDto);
    }

    public AccountDto getVaultByBranchIdAndCurrency(Long branchId, String currency) {
        BranchDto branchDto = branchService.findById(branchId);
        return accountService.findVaultByBranchAndCurrency(branchDto, currency);
    }


    /**
     * Creates general ledger accounts for various branch fees.
     *
     * @param savedBranch The branch for which general ledgers are created.
     */
    private void createGL(BranchDto savedBranch) {

        currentUsername = Helper.getAuthenticated().getName();

        createAndSaveLedger(savedBranch, "GL Withdraw Fees Branch", LedgerType.WITHDRAW);
        createAndSaveLedger(savedBranch, "GL Transfer Fees Branch", LedgerType.TRANSFER);
        createAndSaveLedger(savedBranch, "GL Loan Fees Branch", LedgerType.LOAN);
        createAndSaveLedger(savedBranch, "GL Card Application Fees Branch", LedgerType.CARD_REQUEST);
        createAndSaveLedger(savedBranch, "GL Cheque Application Fees Branch", LedgerType.CHEQUE_REQUEST);
    }


    /**
     * Creates vault accounts in different currencies for a branch.
     *
     * @param savedBranch The branch for which vaults are created.
     * @param vaultsAsset Array of initial balances for each currency vault.
     */
    private void createVaults(BranchDto savedBranch, double[] vaultsAsset) {

        createAndSaveVault(savedBranch, Currency.KMF, vaultsAsset[0]);
        createAndSaveVault(savedBranch, Currency.EUR, vaultsAsset[1]);
        createAndSaveVault(savedBranch, Currency.USD, vaultsAsset[2]);
    }


    /**
     * Helper method to create and save a ledger account for a branch.
     *
     * @param branch Branch associated with the ledger.
     * @param accountName Template for naming the account.
     * @param ledgerType Type of ledger to create.
     */
    private void createAndSaveLedger(BranchDto branch, String accountName, LedgerType ledgerType) {
        AccountDto account = accountService.saveAccount(AccountDto.builder()
                .accountName(String.format(accountName + " %d", branch.getBranchId()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.SYSTEM.name())
                .currency(Currency.KMF.name())
                .branch(branch)
                .status(branch.getStatus())
                .initiatedBy(currentUsername)
                .build());

        accountService.saveLedger(LedgerDto.builder()
                .ledgerType(ledgerType.name())
                .account(account)
                .build());
    }


    /**
     * Helper method to create and save a vault account for a branch.
     *
     * @param branch  Branch associated with the vault.
     * @param currency Currency of the vault account.
     * @param balance Initial balance for the vault account.
     */
    private void createAndSaveVault(BranchDto branch, Currency currency, double balance) {
        accountService.saveAccount(AccountDto.builder()
                .accountName(String.format("Vault %s %d", currency, branch.getBranchId()))
                .accountNumber(Helper.generateAccountNumber())
                .accountType(AccountType.BUSINESS.name())
                .accountProfile(AccountProfile.SYSTEM.name())
                .currency(currency.name())
                .balance(balance)
                .isVault(1)
                .branch(branch)
                .status(branch.getStatus())
                .initiatedBy(currentUsername)
                .build());
    }
}
