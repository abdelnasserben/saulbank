package com.dabel.constant;

public class Bank {

    public static class Fees {

        public interface Card {
            double APPLICATION_REQUEST = 5000;
        }

        public interface Transfer {
            double ONLINE = 525;
        }

        public interface Withdraw {
            double ONLINE = 200;
            double ON_ATM = 300;
        }
    }

    public interface ExchangeCourse {
        double SALE_EUR = 495.1;
        double BUY_EUR = 490.31;
        double SALE_USD = 462.12;
        double BUY_USD = 456.51;
    }
}
