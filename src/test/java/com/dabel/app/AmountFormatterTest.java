package com.dabel.app;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AmountFormatterTest {

    @Test
    void shouldFormatFiveHundred() {
        assertThat(AmountFormatter.format(500.25789)).isEqualTo(500.25);
    }

    @Test
    void shouldFormatSeventyFour() {
        assertThat(AmountFormatter.format(74.94789)).isEqualTo(74.94);
    }

    @Test
    void shouldFormatTwenty() {
        assertThat(AmountFormatter.format(20.025879)).isEqualTo(20.02);
    }
}