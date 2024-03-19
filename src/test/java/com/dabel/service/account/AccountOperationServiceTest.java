package com.dabel.service.account;

import com.dabel.DBSetupForTests;
import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Currency;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AccountOperationServiceTest {

    @Autowired
    AccountOperationService accountOperationService;

    @Autowired
    AccountService accountService;

    @Autowired
    DBSetupForTests dbSetupForTests;

    AccountDto savedAccount;

    @BeforeEach
    void setUp() {
        dbSetupForTests.truncate();
        savedAccount = accountService.save(AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .currency(Currency.KMF.name())
                .accountType(AccountType.SAVING.name())
                .accountProfile(AccountProfile.PERSONAL.name())
                .balance(500)
                .status(Status.ACTIVE.code())
                .build());
    }

    @Test
    void shouldDebitAnAccount() {
        //given
        //when
        accountOperationService.debit(savedAccount, 100);
        AccountDto expected = accountService.findByNumber("123456789");

        //then
        assertThat(expected.getBalance()).isEqualTo(400);
    }

    @Test
    void shouldCreditAnAccount() {
        //given
        //when
        accountOperationService.credit(savedAccount, 100);
        AccountDto expected = accountService.findByNumber("123456789");

        //then
        assertThat(expected.getBalance()).isEqualTo(600);
    }
}