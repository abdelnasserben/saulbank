package com.dabel.service.cheque;

import com.dabel.DBSetupForTests;
import com.dabel.dto.*;
import com.dabel.exception.ResourceNotFoundException;
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
class ChequeServiceTest {

    @Autowired
    ChequeService chequeService;

    @Autowired
    ChequeRequestService chequeRequestService;

    @Autowired
    BranchService branchService;

    @Autowired
    CustomerService customerService;

    @Autowired
    AccountService accountService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private ChequeDto getChequeDto() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());

        AccountDto savedAccount = accountService.save(AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .currency("KMF")
                .balance(25000)
                .accountType("SAVING")
                .accountProfile("PERSONAL")
                .status("1")
                .branch(savedBranch)
                .build());

        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE123456")
                .status("1")
                .branch(savedBranch)
                .build());

        TrunkDto savedTrunk = accountService.save(TrunkDto.builder()
                .account(savedAccount)
                .customer(savedCustomer)
                .membership("OWNER")
                .build());

        ChequeRequestDto requestDto = chequeRequestService.save(ChequeRequestDto.builder()
                .trunk(savedTrunk)
                .status("0")
                .branch(savedBranch)
                .build());

        return ChequeDto.builder()
                .chequeNumber("12345678")
                .serial(requestDto)
                .trunk(savedTrunk)
                .branch(savedBranch)
                .status("0")
                .build();
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldSaveCheque() {
        //given
        //when
        ChequeDto expected = chequeService.save(getChequeDto());

        //then
        assertThat(expected.getChequeId()).isGreaterThan(0);
    }

    @Test
    void shouldFindAllCheques() {
        //given
        chequeService.save(getChequeDto());

        //when
        List<ChequeDto> expected = chequeService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldFindChequeById() {
        //given
        ChequeDto savedCheque = chequeService.save(getChequeDto());

        //when
        ChequeDto expected = chequeService.findById(savedCheque.getChequeId());

        //then
        assertThat(expected.getTrunk().getCustomer().getIdentityNumber()).isEqualTo("NBE123456");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindChequeByNonExistentId() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> chequeService.findById(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Cheque not found");
    }

    @Test
    void shouldFindChequeByNumber() {
        //given
        chequeService.save(getChequeDto());

        //when
        ChequeDto expected = chequeService.findByChequeNumber("12345678");

        //then
        assertThat(expected.getTrunk().getAccount().getAccountNumber()).isEqualTo("123456789");

    }

    @Test
    void shouldThrowExceptionWhenTryingToFindChequeByNonExistentNumber() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> chequeService.findByChequeNumber("fake number"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Cheque not found");
    }
}