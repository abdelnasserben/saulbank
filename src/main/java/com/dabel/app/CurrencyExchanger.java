package com.dabel.app;

import com.dabel.constant.Bank;
import com.dabel.constant.Currency;

public final class CurrencyExchanger {

    public static double exchange(String receivedCurrency, String givenCurrency, double amount) {

        if (isKmf2Eur(receivedCurrency, givenCurrency))
            amount /= Bank.ExchangeCourse.SALE_EUR;

        if (isEur2Kmf(receivedCurrency, givenCurrency))
            amount *= Bank.ExchangeCourse.BUY_EUR;

        if (isKmf2Usd(receivedCurrency, givenCurrency))
            amount /= Bank.ExchangeCourse.SALE_USD;

        if (isUsd2Kmf(receivedCurrency, givenCurrency))
            amount *= Bank.ExchangeCourse.BUY_USD;


        return AmountFormatter.format(amount);
    }

    private static boolean isKmf2Eur(String currency1, String currency2) {
        return currency1.equals(Currency.KMF.name()) && currency2.equals(Currency.EUR.name());
    }

    private static boolean isEur2Kmf(String currency1, String currency2) {
        return currency1.equals(Currency.EUR.name()) && currency2.equals(Currency.KMF.name());
    }

    private static boolean isKmf2Usd(String currency1, String currency2) {
        return currency1.equals(Currency.KMF.name()) && currency2.equals(Currency.USD.name());
    }

    private static boolean isUsd2Kmf(String currency1, String currency2) {
        return currency1.equals(Currency.USD.name()) && currency2.equals(Currency.KMF.name());
    }

}
