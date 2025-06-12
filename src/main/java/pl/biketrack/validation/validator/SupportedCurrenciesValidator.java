package pl.biketrack.validation.validator;

import com.neovisionaries.i18n.CurrencyCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.biketrack.validation.SupportedCurrencies;
import pl.biketrack.validation.util.ValidationUtil;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

public class SupportedCurrenciesValidator implements ConstraintValidator<SupportedCurrencies, CurrencyCode> {

    private SupportedCurrencies constraintAnnotation;

    @Override
    public void initialize(SupportedCurrencies constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(CurrencyCode value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (isNull(value)) {
            return true;
        }

        List<CurrencyCode> validCurrencies = asList(constraintAnnotation.supportedCurrencies());

        if (!validCurrencies.contains(value)) {
            ValidationUtil.addConstraintViolation(context, "Currency [" + value + "] not supported. Supported currencies are: " + validCurrencies);
            return false;
        }

        return true;
    }
}