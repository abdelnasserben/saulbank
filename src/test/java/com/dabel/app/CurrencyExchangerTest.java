package com.dabel.app;

import com.dabel.constant.Currency;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyExchangerTest {

    @Test
    void shouldExchangeTwentyEurToKmf() {
        double expectedAmount = CurrencyExchanger.exchange(Currency.EUR.name(), Currency.KMF.name(), 20);
        assertThat(expectedAmount).isEqualTo(9806.2);
    }

    @Test
    void shouldExchangeKmfToEur() {
        double expectedAmount = CurrencyExchanger.exchange(Currency.KMF.name(), Currency.EUR.name(), 4951);
        assertThat(expectedAmount).isEqualTo(10);
    }

    @Test
    void shouldExchangeUsdToKmf() {
        double expectedAmount = CurrencyExchanger.exchange(Currency.USD.name(), Currency.KMF.name(), 50);
        assertThat(expectedAmount).isEqualTo(22825.5);
    }

    @Test
    void shouldExchangeKmfToUsd() {
        double expectedAmount = CurrencyExchanger.exchange(Currency.KMF.name(), Currency.USD.name(), 9242.4);
        assertThat(expectedAmount).isEqualTo(20);
    }
}