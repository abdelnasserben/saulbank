package com.dabel.app.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardTypeValidatorTest {

    @Test
    void isValidCardType() {
        CardTypeValidator cardTypeValidator = new CardTypeValidator();
        assertTrue(cardTypeValidator.isValid("VISA", null));
    }

    @Test
    void isInvalidCardType() {
        CardTypeValidator cardTypeValidator = new CardTypeValidator();
        assertFalse(cardTypeValidator.isValid("FAKE", null));
    }
}