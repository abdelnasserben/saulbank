package com.dabel.app;

import java.util.Random;

public class Generator {

    public static String generateAccountNumber() {
        Random random = new Random();
        int part1 = random.nextInt(1000);
        int part2 = random.nextInt(10000);
        int part3 = random.nextInt(1000, 10000);

        return String.format("%03d%04d%d", part1, part2, part3);
    }
}
