package com.empresa.onboarding.controller.dto.validation;

import com.bacen.regulatorio.commons.validator.CpfCnpjValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfCnpjValidador implements ConstraintValidator<CpfCnpj, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && CpfCnpjValidator.isValid(value);
    }
}
