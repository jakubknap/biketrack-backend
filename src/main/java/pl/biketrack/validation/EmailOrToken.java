package pl.biketrack.validation;

import jakarta.validation.Constraint;
import pl.biketrack.validation.validator.EmailOrTokenValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {EmailOrTokenValidator.class})
public @interface EmailOrToken {

    String message() default "Only one of email or token should be provided.";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};
}