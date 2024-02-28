package com.dabel.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTest {

    @Test
    void shouldGetApprovedStatusByZeroCode() {
        assertThat(Status.nameOf("3")).isEqualTo(Status.APPROVED.name());
    }

    @Test
    void shouldGetCodeOfActiveStatusByName() {
        assertThat(Status.codeOf("ACTIVE")).isEqualTo(Status.ACTIVE.code());
    }

    @Test
    void shouldGetPendingStatusWhenCodeDoesNotExists() {
        assertThat(Status.nameOf("100")).isEqualTo(Status.PENDING.name());
    }

    @Test
    void shouldGetPendingCodeWhenStatusNameDoesNotExists() {
        assertThat(Status.codeOf("UNKNOWN")).isEqualTo(Status.PENDING.code());
    }
}