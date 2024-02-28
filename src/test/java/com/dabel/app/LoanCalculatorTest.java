package com.dabel.app;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoanCalculatorTest {

    @Test
    void shouldCalculateLoanTotalAmountOf() {
        double expected = LoanCalculator.getTotalAmount(500000, 5.5);
        assertThat(expected).isEqualTo(527500);
    }
}