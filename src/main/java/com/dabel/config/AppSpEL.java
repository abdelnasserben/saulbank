package com.dabel.config;

import com.dabel.app.Helper;
import com.dabel.constant.*;
import com.dabel.dto.TrunkDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public final class AppSpEL {

    public static String statusColor(String status) {

        if(status.equals(Status.PENDING.name()))
            return "warning";

        if(status.equals(Status.COMPLETED.name()))
            return "info";

        return List.of(Status.ACTIVE.name(), Status.APPROVED.name()).contains(status) ? "success" : "danger";
    }

    public static Object[] countries() {
        return Arrays.stream(Country.values())
                .map(Country::getName)
                .toArray();
    }

    public static Object[] userRoles() {
        return Arrays.stream(UserRole.values())
                .map(Enum::name)
                .toArray();
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

    public String currentUsername() {
        return Helper.getAuthenticated().getName();
    }
}
