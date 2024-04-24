package com.dabel.service.fee;

import com.dabel.DBSetupForTests;
import com.dabel.app.Fee;
import com.dabel.constant.LedgerType;
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


    private void saveLedger(LedgerType ledgerType) {
        accountService.save(LedgerDto.builder()
                .ledgerType(ledgerType.name())
                .account(AccountDto.builder()
                        .accountName("Ledger")
                        .accountNumber("0213456587")
                        .accountType("BUSINESS")
                        .accountProfile("PERSONAL")
                        .currency("KMF")
                        .branch(savedBranch)
                        .status("1")
                        .build())
                .build());
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
        savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());

        accountDto = accountService.save(AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .currency("KMF")
                .balance(5000)
                .accountType("SAVING")
                .accountProfile("PERSONAL")
                .status("1")
                .branch(savedBranch)
                .build());
    }

    @Test
    void shouldApplyWithdrawFee() {
        //given
        saveLedger(LedgerType.WITHDRAW);
        Fee fee = new Fee(savedBranch, 200, "Withdraw");

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
        Fee fee = new Fee(savedBranch, 525, "Transfer");

        //when
        feeService.apply(accountDto, LedgerType.TRANSFER, fee);
        AccountDto expected = accountService.findByNumber("123456789");

        //then
        assertThat(expected.getBalance()).isEqualTo(4475);
    }

    @Test
    void shouldApplyLoanFee() {
        //given
        saveLedger(LedgerType.LOAN);
        Fee fee = new Fee(savedBranch, 600, "Loan");

        //when
        feeService.apply(accountDto, LedgerType.LOAN, fee);
        AccountDto expected = accountService.findByNumber("123456789");

        //then
        assertThat(expected.getBalance()).isEqualTo(4400);
    }

    @Test
    void shouldApplyChequeRequestFee() {
        //given
        saveLedger(LedgerType.LOAN);
        Fee fee = new Fee(savedBranch, 300, "Cheque Request");

        //when
        feeService.apply(accountDto, LedgerType.LOAN, fee);
        AccountDto expected = accountService.findByNumber("123456789");

        //then
        assertThat(expected.getBalance()).isEqualTo(4700);
    }

    @Test
    void shouldApplyCardRequestFee() {
        //given
        saveLedger(LedgerType.LOAN);
        Fee fee = new Fee(savedBranch, 425, "Card Request");

        //when
        feeService.apply(accountDto, LedgerType.LOAN, fee);
        AccountDto expected = accountService.findByNumber("123456789");

        //then
        assertThat(expected.getBalance()).isEqualTo(4575);
    }
}