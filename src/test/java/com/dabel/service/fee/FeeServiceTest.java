package com.dabel.service.fee;

import com.dabel.DBSetupForTests;
import com.dabel.app.Fee;
import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.BranchDto;
import com.dabel.dto.LedgerDto;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FeeServiceTest {

    @Autowired
    FeeService feeService;

    @Autowired
    BranchService branchService;

    @Autowired
    AccountService accountService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    BranchDto savedBranch;
    AccountDto accountDto;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
        savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status(Status.ACTIVE.code())
                .build());

        accountDto = accountService.save(AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .currency(Currency.KMF.name())
                .balance(5000)
                .accountType(AccountType.CURRENT.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .status(Status.ACTIVE.code())
                .branch(savedBranch)
                .build());
    }

    @Test
    void shouldApplyWithdrawFee() {
        //given
        saveLedger(LedgerType.WITHDRAW);
        Fee fee = new Fee(savedBranch, Bank.Fees.Withdraw.ONLINE, "Withdraw");

        //when
        feeService.apply(accountDto, LedgerType.WITHDRAW, fee);
        AccountDto expected = accountService.findByNumber("123456789");

        //then
        assertThat(expected.getBalance()).isEqualTo(4800);
    }

    @Test
    void shouldApplyTransferFee() {
        //given
        saveLedger(LedgerType.TRANSFER);
        Fee fee = new Fee(savedBranch, Bank.Fees.Transfer.ONLINE, "Transfer");

        //when
        feeService.apply(accountDto, LedgerType.TRANSFER, fee);
        AccountDto expected = accountService.findByNumber("123456789");

        //then
        assertThat(expected.getBalance()).isEqualTo(4475);
    }

    private void saveLedger(LedgerType ledgerType) {
        accountService.save(LedgerDto.builder()
                .ledgerType(ledgerType.name())
                .account(AccountDto.builder()
                        .accountName("Ledger")
                        .accountNumber("0213456587")
                        .accountType(AccountType.BUSINESS.name())
                        .accountProfile(AccountProfile.PERSONAL.name())
                        .currency(Currency.KMF.name())
                        .branch(savedBranch)
                        .status(Status.ACTIVE.code())
                        .build())
                .build());
    }
}