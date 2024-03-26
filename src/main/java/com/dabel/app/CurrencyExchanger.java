package com.dabel.app;

import com.dabel.constant.BankFees;
import com.dabel.constant.Currency;
import com.dabel.constant.ExchangeType;

public final class CurrencyExchanger {

    public static double exchange(String receivedCurrency, String givenCurrency, double amount) {

        if (isKmf2Eur(receivedCurrency, givenCurrency))
            amount /= BankFees.Exchange.SALE_EUR;

        if (isEur2Kmf(receivedCurrency, givenCurrency))
            amount *= BankFees.Exchange.BUY_EUR;

        if (isKmf2Usd(receivedCurrency, givenCurrency))
            amount /= BankFees.Exchange.SALE_USD;

        if (isUsd2Kmf(receivedCurrency, givenCurrency))
            amount *= BankFees.Exchange.BUY_USD;


        return Helper.formatAmount(amount);
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

    public static ExchangeType getExchangeType(String currency1, String currency2) {

        if(isKmf2Eur(currency1, currency2))
            return ExchangeType.KMF_EUR;

        if(isEur2Kmf(currency1, currency2))
            return ExchangeType.EUR_KMF;

        if(isKmf2Usd(currency1, currency2))
            return ExchangeType.KMF_USD;

        return ExchangeType.USD_KMF;
    }

}
