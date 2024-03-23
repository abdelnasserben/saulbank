package com.dabel.service.loan;

import com.dabel.DBSetupForTests;
import com.dabel.constant.*;
import com.dabel.dto.*;
import com.dabel.exception.IllegalOperationException;
import com.dabel.service.account.AccountService;
import com.dabel.service.branch.BranchService;
import com.dabel.service.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class LoanFacadeServiceTest {

    @Autowired
    LoanFacadeService loanFacadeService;

    @Autowired
    BranchService branchService;

    @Autowired
    CustomerService customerService;

    @Autowired
    AccountService accountService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    private LoanDto getLoanDto() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status(Status.ACTIVE.code())
                .build());
        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status(Status.ACTIVE.code())
                .branch(savedBranch)
                .build());
        return LoanDto.builder()
                .loanType(LoanType.GOLD.name())
                .borrower(savedCustomer)
                .issuedAmount(500000)
                .interestRate(5.5)
                .duration(6)
                .branch(savedBranch)
                .build();
    }

    @Test
    void shouldInitLoan() {
        //given
        //when
        loanFacadeService.init(getLoanDto());
        LoanDto expected = loanFacadeService.findAll().get(0);

        //then
        assertThat(expected.getLoanId()).isGreaterThan(0);
        assertThat(expected.getIssuedAmount()).isEqualTo(500000);
        assertThat(expected.getTotalAmount()).isEqualTo(527500);
        assertThat(expected.getStatus()).isEqualTo(Status.PENDING.code());
    }

    @Test
    void shouldApproveLoan() {
        //given
        loanFacadeService.init(getLoanDto());
        LoanDto savedLoan = loanFacadeService.findAll().get(0);
        accountService.save(LedgerDto.builder()
                .ledgerType(LedgerType.LOAN.name())
                .account(AccountDto.builder()
                        .accountName("Loan ledger")
                        .accountNumber("0213456587")
                        .accountType(AccountType.BUSINESS.name())
                        .accountProfile(AccountProfile.PERSONAL.name())
                        .currency(Currency.KMF.name())
                        .branch(savedLoan.getBranch())
                        .status(Status.ACTIVE.code())
                        .build())
                .build());

        //when
        loanFacadeService.approve(savedLoan.getLoanId());
        LoanDto expected = loanFacadeService.findAll().get(0);

        //then
        assertThat(expected.getStatus()).isEqualTo(Status.ACTIVE.code());
    }

    @Test
    void shouldRejectLoan() {
        //given
        loanFacadeService.init(getLoanDto());
        LoanDto savedLoan = loanFacadeService.findAll().get(0);

        //when
        loanFacadeService.reject(savedLoan.getLoanId(), "Simple remarks");
        LoanDto expected = loanFacadeService.findAll().get(0);

        //then
        assertThat(expected.getStatus()).isEqualTo(Status.REJECTED.code());
        assertThat(expected.getFailureReason()).isEqualTo("Simple remarks");
    }

    @Test
    void shouldFindAllLoans() {
        //given
        loanFacadeService.init(getLoanDto());

        //when
        List<LoanDto> expected = loanFacadeService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldFindLoanById() {
        //given
        loanFacadeService.init(getLoanDto());
        LoanDto savedLoan = loanFacadeService.findAll().get(0);

        //when
        LoanDto expected = loanFacadeService.findById(savedLoan.getLoanId());

        //then
        assertThat(expected.getLoanType()).isEqualTo(LoanType.GOLD.name());
        assertThat(expected.getStatus()).isEqualTo(Status.PENDING.code());
    }

    @Test
    void shouldFindCustomerLoansByHisIdentity() {
        //given
        loanFacadeService.init(getLoanDto());

        //when
        List<LoanDto> expected = loanFacadeService.findAllByCustomerIdentityNumber("NBE123456");

        //then
        assertThat(expected.size()).isEqualTo(1);
        assertThat(expected.get(0).getLoanType()).isEqualTo(LoanType.GOLD.name());
        assertThat(expected.get(0).getStatus()).isEqualTo(Status.PENDING.code());
    }

    @Test
    void shouldThrowAnIllegalOperationExceptionWhenTryInitLoanOfAnInactiveCustomer() {
        //given
        LoanDto loanDto = LoanDto.builder()
                .loanType(LoanType.SALARY.name())
                .issuedAmount(500)
                .interestRate(2.3)
                .borrower(CustomerDto.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .identityNumber("UK45821")
                        .status(Status.PENDING.code())
                        .build())
                .build();

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> loanFacadeService.init(loanDto));

        //then
        assertThat(expected.getMessage()).isEqualTo("Customer must be active");
    }
}