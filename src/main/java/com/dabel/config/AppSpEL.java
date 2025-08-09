package com.dabel.config;

import com.dabel.app.Helper;
import com.dabel.constant.*;
import com.dabel.dto.TrunkDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public final class AppSpEL {

    public static String statusColor(String status) {

        if(status.equals(Status.PENDING.name()))
            return "warning";

        if(status.equals(Status.COMPLETED.name()))
            return "info";

        return List.of(Status.ACTIVE.name(), Status.APPROVED.name(), Status.SUCCESS.name()).contains(status) ? "success" : "danger";
    }

    public static String httpStatusColor(String status) {

        if(status.equals(String.valueOf(HttpStatus.OK)))
            return "success";

        if(status.equals(String.valueOf(HttpStatus.BAD_REQUEST)))
            return "warning";

        if(status.equals(String.valueOf(HttpStatus.FORBIDDEN)))
            return "danger";

        return "info";
    }

    public static Object[] countries() {
        return Arrays.stream(Country.values())
                .map(Country::getName)
                .toArray();
    }

    public static UserRole[] userRoles() {
        return UserRole.values();
    }

    public static Object[] currencies() {
        return Arrays.stream(Currency.values())
                .map(Enum::name)
                .toArray();
    }

    public static Object[] cardTypes() {
        return Arrays.stream(CardType.values())
                .map(Enum::name)
                .toArray();
    }

    public static boolean removableMember(TrunkDto trunkDto) {

        return trunkDto.getAccount().getAccountProfile().equals(AccountProfile.ASSOCIATIVE.name())
                || trunkDto.getAccount().getAccountProfile().equals(AccountProfile.JOINT.name())
                && !trunkDto.getMembership().equals(AccountMembership.OWNER.name());
    }

    public static String elapsedTime(LocalDateTime dateTime) {
        return Optional.ofNullable(dateTime).map(Helper::elapsedTime).orElse("");
    }

    public static String formatAmount(double value) {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');

        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        return df.format(value);
    }
}
