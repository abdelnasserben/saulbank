package com.dabel.app;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.stream.IntStream;

public class CardExpirationDateUtils {

    public static int[] getMonths() {
        return IntStream.rangeClosed(1, 12).toArray();
    }

    public static int[] getYears() {
        LocalDate currentDate = LocalDate.now(Clock.systemUTC());
        return IntStream.range(currentDate.getYear(), currentDate.plusYears(10).getYear()).toArray();
    }

    public static boolean isValidExpiryDate(int year, int month) {
        YearMonth expirationDate = YearMonth.of(year, month);
        return expirationDate.isAfter(YearMonth.now(Clock.systemUTC()));
    }

    public static LocalDate getDate(int year, int month) {
        LocalDate dateWithFirstDayOfTheMonth = LocalDate.of(year, month, 1);
        return LocalDate.of(year, month, dateWithFirstDayOfTheMonth.lengthOfMonth());
    }
}
