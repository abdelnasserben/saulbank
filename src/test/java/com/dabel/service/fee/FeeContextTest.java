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
    void shouldSetWithdrawFeeContext() {
        Tax expected = feeContext.setContext("WITHDRAW");
        assertThat(expected.getLedgerType()).isEqualTo(LedgerType.WITHDRAW);
    }

    @Test
    void shouldSetTransferFeeContext() {
        Tax expected = feeContext.setContext("TRANSFER");
        assertThat(expected.getLedgerType()).isEqualTo(LedgerType.TRANSFER);
    }

    @Test
    void shouldSetLoanFeeContext() {
        Tax expected = feeContext.setContext("LOAN");
        assertThat(expected.getLedgerType()).isEqualTo(LedgerType.LOAN);
    }

    @Test
    void shouldSetChequeRequestFeeContext() {
        Tax expected = feeContext.setContext("CHEQUE_REQUEST");
        assertThat(expected.getLedgerType()).isEqualTo(LedgerType.CHEQUE_REQUEST);
    }

    @Test
    void shouldSetCardRequestFeeContext() {
        Tax expected = feeContext.setContext("CARD_REQUEST");
        assertThat(expected.getLedgerType()).isEqualTo(LedgerType.CARD_REQUEST);
    }

    @Test
    void shouldThrowWhenTryingToSetANonExistentContextType() {
        Exception expected = assertThrows(IllegalOperationException.class, () -> feeContext.setContext("FAKE TYPE"));
        assertThat(expected.getMessage()).isEqualTo("Unknown fee type");
    }
}