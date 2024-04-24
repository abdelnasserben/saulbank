package com.dabel.service.loan;

import com.dabel.DBSetupForTests;
import com.dabel.dto.BranchDto;
import com.dabel.dto.CustomerDto;
import com.dabel.dto.LoanDto;
import com.dabel.exception.ResourceNotFoundException;
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
class LoanServiceTest {

    @Autowired
    LoanService loanService;

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
    void shouldSaveLoan() {
        //given
        //when
        LoanDto expected = loanService.save(getLoanDto());

        //then
        assertThat(expected.getLoanId()).isGreaterThan(0);
    }

    @Test
    void shouldFindAllLoans() {
        //given
        loanService.save(getLoanDto());

        //when
        List<LoanDto> expected = loanService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldFindLoanById() {
        //given
        LoanDto savedLoan = loanService.save(getLoanDto());

        //when
        LoanDto expected = loanService.findById(savedLoan.getLoanId());

        //then
        assertThat(expected.getBorrower().getIdentityNumber()).isEqualTo("NBE123456");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindLoanByNonExistentId() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> loanService.findById(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Loan not found");
    }

    @Test
    void shouldFindBorrowersLoansByIdentityNumber() {
        //given
        loanService.save(getLoanDto());

        //when
        List<LoanDto> expected = loanService.findAllByCustomerIdentity("NBE123456");

        //then
        assertThat(expected.size()).isEqualTo(1);
    }
}