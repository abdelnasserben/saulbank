package com.dabel.constant;

public class Bank {

    public interface Fees {

        double CARD_APPLICATION_REQUEST = 5000;

        double TRANSFER = 525;

        double WITHDRAW = 200;

        double EXCHANGE = 0;
    }

    public interface ExchangeCourse {
        double SALE_EUR = 495.1;
        double BUY_EUR = 490.31;
        double SALE_USD = 462.12;
        double BUY_USD = 456.51;
    }
}
