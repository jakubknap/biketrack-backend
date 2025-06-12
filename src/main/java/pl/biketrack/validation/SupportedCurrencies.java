package pl.biketrack.validation;

import com.neovisionaries.i18n.CurrencyCode;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.biketrack.validation.validator.SupportedCurrenciesValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = {SupportedCurrenciesValidator.class})
@Documented
public @interface SupportedCurrencies {

    String message() default "{}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    CurrencyCode[] supportedCurrencies();
}