package com.dabel.service.fee;

import com.dabel.constant.LedgerType;
import com.dabel.exception.IllegalOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FeeContextTest {

    @Autowired
    FeeContext feeContext;

    @Test
    void shouldSetWithdrawFeetContext() {
        Tax expected = feeContext.setContext("WITHDRAW");
        assertThat(expected.getLedgerType()).isEqualTo(LedgerType.WITHDRAW);
    }

    @Test
    void shouldSetTransferFeeContext() {
        Tax expected = feeContext.setContext("TRANSFER");
        assertThat(expected.getLedgerType()).isEqualTo(LedgerType.TRANSFER);
    }

    @Test
    void shouldThrowIllegalOperationExceptionWhenContextTypeDoesNotExists() {
        Exception expected = assertThrows(IllegalOperationException.class, () -> feeContext.setContext("FAKE TYPE"));
        assertThat(expected.getMessage()).isEqualTo("Unknown fee type");
    }
}