package pl.biketrack.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.biketrack.validation.validator.CostCurrencyValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {CostCurrencyValidator.class})
@Documented
public @interface CostCurrencyConsistency {

    String message() default "Cost and currency must be specified together or both left null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}