package com.dabel.service.loan;

import com.dabel.DBSetupForTests;
import com.dabel.dto.*;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import com.dabel.service.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class LoanOperationServiceTest {

    @Autowired
    LoanOperationService loanOperationService;

    @Autowired
    AccountService accountService;

    @Autowired
    BranchService branchService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private LoanDto getLoanDto() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status("1")
                .branch(savedBranch)
                .build());
        return LoanDto.builder()
                .loanType("GOLD")
                .borrower(savedCustomer)
                .issuedAmount(500000)
                .interestRate(5.5)
                .duration(6)
                .branch(savedBranch)
                .build();
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldGetLoanService() {
        assertThat(loanOperationService.getLoanService()).isNotNull();
    }

    @Test
    void shouldInitiateLoan() {
        //given
        LoanDto loanDto = getLoanDto();

        //when
        loanOperationService.init(loanDto);
        LoanDto expected = loanOperationService.getLoanService().findAll().get(0);

        //then
        assertThat(expected.getLoanId()).isGreaterThan(0);
        assertThat(expected.getStatus()).isEqualTo("0");
    }

    @Test
    void shouldThrowExceptionWhenInitiatingLoanWithAnInactiveBorrower() {
        //given
        LoanDto loanDto = getLoanDto();
        loanDto.getBorrower().setStatus("5"); //deactivated status = 5
        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> loanOperationService.init(loanDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Borrower must be active");
    }

    @Test
    void shouldApproveLoan() {
        //given
        LoanDto loanDto = getLoanDto();

        //TODO: save ledger for loan fees
        accountService.save(LedgerDto.builder()
                .ledgerType("LOAN")
                .account(AccountDto.builder()
                        .accountNumber("Loan ledger")
                        .accountNumber("123456789")
                        .accountType("BUSINESS")
                        .accountProfile("PERSONAL")
                        .currency("KMF")
                        .status("1")
                        .branch(loanDto.getBranch())
                        .build())
                .branch(loanDto.getBranch())
                .build());

        loanOperationService.init(loanDto);
        LoanDto initiatedLoan = loanOperationService.getLoanService().findAll().get(0);

        //when
        loanOperationService.approve(initiatedLoan);

        //then
        assertThat(initiatedLoan.getStatus()).isEqualTo("1"); //loan approval make status active = 1
    }

    @Test
    void shouldRejectLoan() {

        loanOperationService.init(getLoanDto());
        LoanDto initiatedLoan = loanOperationService.getLoanService().findAll().get(0);

        //when
        loanOperationService.reject(initiatedLoan, "Just a reject reason");

        //then
        assertThat(initiatedLoan.getStatus()).isEqualTo("4"); //Rejected status = 4
        assertThat(initiatedLoan.getFailureReason()).isEqualTo("Just a reject reason");
    }
}