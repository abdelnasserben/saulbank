package com.dabel.app;

import com.dabel.constant.AccountProfile;
import com.dabel.constant.Status;
import com.dabel.dto.AccountDto;
import com.dabel.dto.CustomerDto;

public final class Checker {

    public static boolean isInactiveAccount(AccountDto accountDto) {
        return !accountDto.getStatus().equals(Status.ACTIVE.name()) && !accountDto.getStatus().equals(Status.ACTIVE.code());
    }

    public static boolean isInActiveCustomer(CustomerDto customerDto) {
        return !customerDto.getStatus().equals(Status.ACTIVE.name()) && !customerDto.getStatus().equals(Status.ACTIVE.code());
    }

    public static boolean isAssociativeAccount(AccountDto accountDto) {
        return accountDto.getAccountProfile().equals(AccountProfile.ASSOCIATIVE.name());
    }
}
