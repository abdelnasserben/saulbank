package com.dabel.service.cheque;

import com.dabel.constant.AccountType;
import com.dabel.exception.IllegalOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ChequeRequestContextTest {

    @Autowired
    ChequeRequestContext chequeRequestContext;

    @Test
    void shouldSetChequeRequestContextOfSavingAccount() {
        ChequeRequest expected = chequeRequestContext.setContext("SAVING");
        assertThat(expected.getType()).isEqualTo(AccountType.SAVING);
    }

    @Test
    void shouldSetChequeRequestContextOfBusinessAccount() {
        ChequeRequest expected = chequeRequestContext.setContext("BUSINESS");
        assertThat(expected.getType()).isEqualTo(AccountType.BUSINESS);
    }

    @Test
    void shouldThrowWhenTryingToSetChequeRequestContextOfANonExistentAccountType() {
        Exception expected = assertThrows(IllegalOperationException.class, () -> chequeRequestContext.setContext("FAKE TYPE"));
        assertThat(expected.getMessage()).isEqualTo("Unknown cheque application type");
    }
}