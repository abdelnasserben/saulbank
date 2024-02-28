package com.dabel.app;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratorTest {

    @Test
    void shouldGenerateAccountNumberOfElevenCharacters() {
        assertThat(Generator.generateAccountNumber().length()).isEqualTo(11);
    }
}