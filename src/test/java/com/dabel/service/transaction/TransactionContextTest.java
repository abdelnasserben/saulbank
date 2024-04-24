package com.dabel.service.transaction;

import com.dabel.constant.TransactionType;
import com.dabel.exception.IllegalOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TransactionContextTest {

    @Autowired
    TransactionContext transactionContext;

    @Test
    void shouldSetDepositContext() {
        Transaction expected = transactionContext.setContext("DEPOSIT");
        assertThat(expected.getType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void shouldSetWithdrawContext() {
        Transaction expected = transactionContext.setContext("WITHDRAW");
        assertThat(expected.getType()).isEqualTo(TransactionType.WITHDRAW);
    }

    @Test
    void shouldSetTransferContext() {
        Transaction expected = transactionContext.setContext("TRANSFER");
        assertThat(expected.getType()).isEqualTo(TransactionType.TRANSFER);
    }

    @Test
    void shouldSetChequePaymentContext() {
        Transaction expected = transactionContext.setContext("CHEQUE_PAYMENT");
        assertThat(expected.getType()).isEqualTo(TransactionType.CHEQUE_PAYMENT);
    }

    @Test
    void shouldThrowWhenTryingToSetANonExistentContextType() {
        Exception expected = assertThrows(IllegalOperationException.class, () -> transactionContext.setContext("FAKE TYPE"));
        assertThat(expected.getMessage()).isEqualTo("Unknown transaction type");
    }
}