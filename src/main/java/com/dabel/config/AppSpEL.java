package com.dabel.config;

import com.dabel.constant.*;
import com.dabel.dto.AccountDto;
import com.dabel.dto.TrunkDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AppSpEL {

    public String statusColor(String status) {

        if(status.equals(Status.PENDING.name()))
            return "warning";

        return List.of(Status.ACTIVE.name(), Status.APPROVED.name()).contains(status) ? "success" : "danger";
    }

    public Object[] countries() {
        return Arrays.stream(Country.values())
                .map(Country::getName)
                .toArray();
    }

    public Object[] currencies() {
        return Arrays.stream(Currency.values())
                .map(Enum::name)
                .toArray();
    }

    public Object[] cardTypes() {
        return Arrays.stream(CardType.values())
                .map(Enum::name)
                .toArray();
    }

    public boolean removableMember(TrunkDto trunkDto) {

        return trunkDto.getAccount().getAccountProfile().equals(AccountProfile.ASSOCIATIVE.name())
                || trunkDto.getAccount().getAccountProfile().equals(AccountProfile.JOINT.name())
                && !trunkDto.getMembership().equals(AccountMembership.OWNER.name());
    }
}
