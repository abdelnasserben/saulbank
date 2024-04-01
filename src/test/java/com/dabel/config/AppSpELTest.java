package com.dabel.config;

import com.dabel.constant.AccountMembership;
import com.dabel.constant.AccountProfile;
import com.dabel.dto.AccountDto;
import com.dabel.dto.TrunkDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class AppSpELTest {

    @Test
    void shouldRemoveJointedMemberOnJointedAccount() {
        TrunkDto trunkDto = TrunkDto.builder()
                .account(AccountDto.builder()
                        .accountNumber("123456789")
                        .accountProfile(AccountProfile.JOINT.name())
                        .build())
                .membership(AccountMembership.JOINTED.name())
                .build();

        AppSpEL appSpEL = new AppSpEL();

        assertTrue(appSpEL.removableMember(trunkDto));
    }

    @Test
    void shouldNotRemoveOwnerOfJointedAccount() {
        TrunkDto trunkDto = TrunkDto.builder()
                .account(AccountDto.builder()
                        .accountNumber("123456789")
                        .accountProfile(AccountProfile.JOINT.name())
                        .build())
                .membership(AccountMembership.OWNER.name())
                .build();

        AppSpEL appSpEL = new AppSpEL();

        assertFalse(appSpEL.removableMember(trunkDto));
    }

    @Test
    void shouldRemoveAssociatedMemberOnAssociativeAccount() {
        TrunkDto trunkDto = TrunkDto.builder()
                .account(AccountDto.builder()
                        .accountNumber("123456789")
                        .accountProfile(AccountProfile.ASSOCIATIVE.name())
                        .build())
                .membership(AccountMembership.ASSOCIATED.name())
                .build();

        AppSpEL appSpEL = new AppSpEL();

        assertTrue(appSpEL.removableMember(trunkDto));
    }

    @Test
    void shouldNotRemoveOwner() {
        TrunkDto trunkDto = TrunkDto.builder()
                .account(AccountDto.builder()
                        .accountNumber("123456789")
                        .accountProfile(AccountProfile.PERSONAL.name())
                        .build())
                .membership(AccountMembership.OWNER.name())
                .build();

        AppSpEL appSpEL = new AppSpEL();

        assertFalse(appSpEL.removableMember(trunkDto));
    }
}