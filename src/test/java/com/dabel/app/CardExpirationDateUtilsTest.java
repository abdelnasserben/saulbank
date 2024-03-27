package com.dabel.app;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardExpirationDateUtilsTest {

    @Test
    void getMonths() {
        int[] months = CardExpirationDateUtils.getMonths();

        assertThat(months.length).isEqualTo(12);
        assertThat(months[0]).isEqualTo(1);
        assertThat(months[11]).isEqualTo(12);
    }

    @Test
    void getYears() {
        int[] years = CardExpirationDateUtils.getYears();

        assertThat(years.length).isEqualTo(10);
        assertThat(years[0]).isEqualTo(2024);
        assertThat(years[9]).isEqualTo(2033);
    }

    @Test
    void nextIsValidExpiryDate() {
        int year = 2025;
        int month = 2;

        assertTrue(CardExpirationDateUtils.isValidExpiryDate(year, month));
    }

    @Test
    void actualYearAndMonthIsInValidExpiryDate() {
        int year = 2024;
        int month = 3;

        assertFalse(CardExpirationDateUtils.isValidExpiryDate(year, month));
    }

    @Test
    void lastYearIsInValidExpiryDate() {
        int year = 2023;
        int month = 12;

        assertFalse(CardExpirationDateUtils.isValidExpiryDate(year, month));
    }

    @Test
    void getDateWithTheMaxDayOfMonthByYearAndMonth() {
        LocalDate expected = CardExpirationDateUtils.getDate(2024, 2);
        assertThat(expected.getMonth().maxLength()).isEqualTo(29);
    }
}