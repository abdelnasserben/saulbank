package com.dabel.app.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AffiliationTypeValidatorTest {

    @Test
    void addIsValidAffiliationType() {
        AffiliationTypeValidator affiliationTypeValidator = new AffiliationTypeValidator();
        assertTrue(affiliationTypeValidator.isValid("ADD", null));
    }

    @Test
    void removeIsValidAffiliationType() {
        AffiliationTypeValidator affiliationTypeValidator = new AffiliationTypeValidator();
        assertTrue(affiliationTypeValidator.isValid("REMOVE", null));
    }

    @Test
    void fakeIsInvalidAffiliationType() {
        AffiliationTypeValidator affiliationTypeValidator = new AffiliationTypeValidator();
        assertFalse(affiliationTypeValidator.isValid("FAKE", null));
    }

}