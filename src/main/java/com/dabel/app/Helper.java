package com.dabel.app;

import com.dabel.constant.AccountProfile;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CardDto;
import com.dabel.dto.CustomerDto;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

public final class Helper {

    public static boolean isInactiveAccount(AccountDto accountDto) {
        return !accountDto.getStatus().equals(Status.ACTIVE.name()) && !accountDto.getStatus().equals(Status.ACTIVE.code());
    }

    public static boolean isInactiveCustomer(CustomerDto customerDto) {
        return !customerDto.getStatus().equals(Status.ACTIVE.name()) && !customerDto.getStatus().equals(Status.ACTIVE.code());
    }

    public static boolean isAssociativeAccount(AccountDto accountDto) {
        return accountDto.getAccountProfile().equals(AccountProfile.ASSOCIATIVE.name());
    }

    public static String hideCardNumber(String cardNumber) {
        return new StringBuilder(cardNumber).replace(0, cardNumber.length() - 4, "****").toString();
    }

    public static double calculateTotalAmountOfLoan(double requestedAmount, double interestRate) {

        DecimalFormat decimalFormat = new DecimalFormat("##.##", new DecimalFormatSymbols(Locale.US));
        decimalFormat.setRoundingMode(RoundingMode.UP);

        double totalAmount = requestedAmount * (1 + interestRate / 100);

        return Double.parseDouble(decimalFormat.format(totalAmount));
    }

    public static double formatAmount(double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        return Double.parseDouble(decimalFormat.format(amount));
    }

    public static String generateAccountNumber() {
        Random random = new Random();
        int part1 = random.nextInt(1000);
        int part2 = random.nextInt(10000);
        int part3 = random.nextInt(1000, 10000);

        return String.format("%03d%04d%d", part1, part2, part3);
    }

    public static boolean isActiveCard(CardDto cardDto) {
        return cardDto.getStatus().equals(Status.ACTIVE.code()) || cardDto.getStatus().equals(Status.ACTIVE.name());
    }
}
