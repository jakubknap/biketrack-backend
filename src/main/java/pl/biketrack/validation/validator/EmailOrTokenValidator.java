package pl.biketrack.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.biketrack.authentication.dto.request.ResendTokenRequest;
import pl.biketrack.validation.EmailOrToken;

import static java.util.Objects.isNull;

public class EmailOrTokenValidator implements ConstraintValidator<EmailOrToken, ResendTokenRequest> {

    @Override
    public boolean isValid(ResendTokenRequest value, ConstraintValidatorContext context) {
        return isNull(value.email()) != isNull(value.expiredToken());
    }
}