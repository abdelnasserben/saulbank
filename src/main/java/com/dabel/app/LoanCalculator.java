package com.dabel.app;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class LoanCalculator {

    public static double getTotalAmount(double requestedAmount, double interestRate) {

        DecimalFormat decimalFormat = new DecimalFormat("##.##", new DecimalFormatSymbols(Locale.US));
        decimalFormat.setRoundingMode(RoundingMode.UP);

        double totalAmount = requestedAmount * (1 + interestRate / 100);

        return Double.parseDouble(decimalFormat.format(totalAmount));
    }

}
