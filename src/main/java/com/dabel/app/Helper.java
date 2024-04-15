package com.dabel.app;

import com.dabel.constant.AccountProfile;
import com.dabel.constant.AccountType;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.StatedObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

public final class Helper {

    public static <T extends StatedObject> boolean isActiveStatedObject(T t) {
        return t.getStatus().equals(Status.ACTIVE.code()) || t.getStatus().equals(Status.ACTIVE.name());
    }

    public static boolean isSavingAccount(AccountDto accountDto) {
        return accountDto.getAccountType().equals(AccountType.SAVING.name());
    }

    public static boolean isBusinessAccount(AccountDto accountDto) {
        return accountDto.getAccountType().equals(AccountType.BUSINESS.name());
    }

    public static boolean isPersonalAccount(AccountDto accountDto) {
        return accountDto.getAccountProfile().equals(AccountProfile.PERSONAL.name());
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
        int part1 = random.nextInt(100);
        int part2 = random.nextInt(10000);
        int part3 = random.nextInt(1000, 10000);

        return String.format("%02d%04d%d", part1, part2, part3);
    }

    public static String generateChequeNumber() {
        Random random = new Random();
        int part1 = random.nextInt(100);
        int part2 = random.nextInt(1000);
        int part3 = random.nextInt(100, 1000);

        return String.format("%02d%03d%03d", part1, part2, part3);
    }
}
