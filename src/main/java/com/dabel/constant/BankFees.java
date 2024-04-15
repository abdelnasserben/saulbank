package com.dabel.constant;

public final class BankFees {

    public interface Basic {
        double CARD_REQUEST = 5000;
        double TRANSFER = 525;
        double WITHDRAW = 200;
        double SAVING_CHEQUE = 1750;
        double BUSINESS_CHEQUE = 3500;
    }

    public interface Exchange {
        double SALE_EUR = 495.1;
        double BUY_EUR = 490.31;
        double SALE_USD = 462.12;
        double BUY_USD = 456.51;
    }
}
