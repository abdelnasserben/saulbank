package com.dabel.app;

import com.dabel.constant.AccountProfile;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelperTest {

    @Test
    void isInactiveAccount() {
        AccountDto accountDto = AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .status(Status.PENDING.code())
                .build();
        assertThat(Helper.isInactiveAccount(accountDto)).isEqualTo(true);
    }

    @Test
    void isActiveAccount() {
        AccountDto accountDto = AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .status(Status.ACTIVE.code())
                .build();
        assertThat(Helper.isInactiveAccount(accountDto)).isEqualTo(false);
    }

    @Test
    void isInActiveCustomer() {
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .status(Status.DEACTIVATED.code())
                .build();

        assertThat(Helper.isInactiveCustomer(customerDto)).isEqualTo(true);
    }

    @Test
    void isActiveCustomer() {
        CustomerDto customerDto = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .status(Status.ACTIVE.code())
                .build();

        assertThat(Helper.isInactiveCustomer(customerDto)).isEqualTo(false);
    }

    @Test
    void isAssociativeAccount() {
        AccountDto accountDto = AccountDto.builder()
                .accountName("John Doe")
                .accountNumber("123456789")
                .accountProfile(AccountProfile.ASSOCIATIVE.name())
                .build();
        assertThat(Helper.isAssociativeAccount(accountDto)).isEqualTo(true);
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
        assertThat(Helper.generateAccountNumber().length()).isEqualTo(11);
    }
}