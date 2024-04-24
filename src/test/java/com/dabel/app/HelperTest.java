package com.dabel.app;

import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CardDto;
import com.dabel.dto.CustomerDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelperTest {

    @Test
    void isInactiveAccount() {
        AccountDto accountDto = AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .status(Status.PENDING.code())
                .build();

        assertFalse(Helper.isActiveStatedObject(accountDto));
    }

    @Test
    void isActiveAccount() {
        AccountDto accountDto = AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .status(Status.ACTIVE.code())
                .build();

        assertTrue(Helper.isActiveStatedObject(accountDto));
    }

    @Test
    void isInactiveCustomer() {
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .status(Status.DEACTIVATED.code())
                .build();

        assertFalse(Helper.isActiveStatedObject(customerDto));
    }

    @Test
    void isActiveCustomer() {
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .status(Status.ACTIVE.code())
                .build();

        assertTrue(Helper.isActiveStatedObject(customerDto));
    }

    @Test
    void isBusinessAccount() {
        AccountDto accountDto = AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .accountType(AccountType.BUSINESS.name())
                .build();

        assertTrue(Helper.isBusinessAccount(accountDto));
    }

    @Test
    void isAssociativeAccount() {
        AccountDto accountDto = AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .accountProfile(AccountProfile.ASSOCIATIVE.name())
                .build();

        assertTrue(Helper.isAssociativeAccount(accountDto));
    }

    @Test
    void hideCardNumber() {
        String accountNumber = "4012888888881881";
        assertThat(Helper.hideCardNumber(accountNumber)).isEqualTo("****1881");
    }

    @Test
    void calculateTotalAmountOfLoan() {
        double expected = Helper.calculateTotalAmountOfLoan(500000, 5.5);
        assertThat(expected).isEqualTo(527500);
    }

    @Test
    void formatAmount() {
        assertThat(Helper.formatAmount(500.25789)).isEqualTo(500.25);
    }

    @Test
    void generateAccountNumber() {
        assertThat(Helper.generateAccountNumber().length()).isEqualTo(10);
    }

    @Test
    void isActiveCard() {
        CardDto cardDto = CardDto.builder()
                .cardName("John Doe")
                .cardNumber("4111 1111 1111 1111")
                .status(Status.ACTIVE.code())
                .build();

        assertTrue(Helper.isActiveStatedObject(cardDto));
    }

    @Test
    void isInactiveCard() {
        CardDto cardDto = CardDto.builder()
                .cardName("John Doe")
                .cardNumber("4111 1111 1111 1111")
                .status(Status.PENDING.code())
                .build();

        assertFalse(Helper.isActiveStatedObject(cardDto));
    }
}