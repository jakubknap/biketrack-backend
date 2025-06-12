package pl.biketrack.validation.util;

import jakarta.validation.ConstraintValidatorContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtil {

    public static void addConstraintViolation(ConstraintValidatorContext context, String msg) {
        context.buildConstraintViolationWithTemplate(msg)
               .addConstraintViolation();
    }
}