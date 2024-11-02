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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CardServiceTest {

    @Autowired
    CardService cardService;

    @Autowired
    BranchService branchService;

    @Autowired
    AccountService accountService;

    @Autowired
    CustomerService customerService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
    }


    private CardDto getCardDto() {
        BranchDto savedBranch = branchService.save(BranchDto.builder()
                .branchName("HQ")
                .branchAddress("London")
                .status("1")
                .build());

        AccountDto savedAccount = accountService.saveAccount(AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .accountType("SAVING")
                .accountProfile("PERSONAL")
                .currency("KMF")
                .branch(savedBranch)
                .build());

        CustomerDto savedCustomer = customerService.save(CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .identityNumber("NBE451287")
                .status("1")
                .branch(savedBranch)
                .build());

        TrunkDto savedTrunk = accountService.saveTrunk(TrunkDto.builder()
                .customer(savedCustomer)
                .account(savedAccount)
                .membership("OWNER")
                .build());

        return CardDto.builder()
                .cardName("John Doe")
                .cardNumber("1450 1525 1542 1248")
                .trunk(savedTrunk)
                .expirationDate(LocalDate.of(24, 2, 26))
                .cvc("123")
                .build();
    }

    @Test
    void shouldSaveCard() {
        //given
        //when
        CardDto expected = cardService.save(getCardDto());

        //then
        assertThat(expected.getCardId()).isGreaterThan(0);
    }

    @Test
    void shouldFindAllCards() {
        //given
        cardService.save(getCardDto());

        //when
        List<CardDto> expected = cardService.findAll();

        //then
        assertThat(expected.size()).isEqualTo(1);
    }

    @Test
    void shouldFindCardById() {
        //given
        CardDto savedCard = cardService.save(getCardDto());

        //when
        CardDto expected = cardService.findById(savedCard.getCardId());

        //then
        assertThat(expected.getTrunk().getCustomer().getIdentityNumber()).isEqualTo("NBE451287");
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindCardByNonExistentId() {
        //given
        //when
        Exception expected = assertThrows(ResourceNotFoundException.class, () -> cardService.findById(1L));

        //then
        assertThat(expected.getMessage()).isEqualTo("Card not found");
    }

    @Test
    void shouldFindTrunkCards() {
        //given
        CardDto savedCard = cardService.save(getCardDto());

        //when
        List<CardDto> expected = cardService.findAllByTrunk(savedCard.getTrunk());

        //then
        assertThat(expected.size()).isEqualTo(1);
    }
}