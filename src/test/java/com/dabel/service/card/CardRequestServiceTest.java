package com.dabel.service.card;

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
class CardRequestServiceTest {

    @Autowired
    CardRequestService cardRequestService;

    @Autowired
    BranchService branchService;

    @Autowired
    CustomerService customerService;

    @Autowired
    AccountService accountService;

    @Autowired
    DBSetupForTests dbSetupForTests;


    private CardRequestDto getCardRequestDto() {
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

        return CardRequestDto.builder()
                .trunk(savedTrunk)
                .cardType("VISA")
                .status("0")
                .branch(savedBranch)
                .build();
    }

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }

    @Test
    void shouldSaveCardRequest() {
        //given
        //when
        CardRequestDto expected = cardRequestService.save(getCardRequestDto());

        //then
        assertThat(expected.getRequestId()).isGreaterThan(0);
    }

    @Test
    void shouldFindAllCardRequests() {
        //given
        cardRequestService.save(getCardRequestDto());

        //when
        List<CardRequestDto> expected = cardRequestService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldFindCardRequestById() {
        //given
        CardRequestDto savedRequest = cardRequestService.save(getCardRequestDto());

        //when
        CardRequestDto expected = cardRequestService.findById(savedRequest.getRequestId());

        //then
        assertThat(expected.getTrunk().getCustomer().getIdentityNumber()).isEqualTo("NBE123456");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindCardRequestByNonExistentId() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> cardRequestService.findById(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Card request not found");
    }
}