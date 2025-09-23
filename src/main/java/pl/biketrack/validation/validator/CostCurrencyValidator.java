package pl.biketrack.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import pl.biketrack.validation.CostCurrencyConsistency;

import java.lang.reflect.Method;

import static java.util.Objects.isNull;

@Slf4j
public class CostCurrencyValidator implements ConstraintValidator<CostCurrencyConsistency, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (isNull(value)) {
            return true;
        }

        try {
            Method costGetter = value.getClass().getMethod("cost");
            Method currencyGetter = value.getClass().getMethod("currency");

            Object costValue = costGetter.invoke(value);
            Object currencyValue = currencyGetter.invoke(value);

            boolean costIsNull = isNull(costValue);
            boolean currencyIsNull = isNull(currencyValue);

            boolean valid = (costIsNull && currencyIsNull) || (!costIsNull && !currencyIsNull);

            if (!valid) {
                context.disableDefaultConstraintViolation();

                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                       .addPropertyNode("cost")
                       .addConstraintViolation();

                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                       .addPropertyNode("currency")
                       .addConstraintViolation();
            }

            return valid;
        } catch (Exception e) {
            log.error("Error while validating cost and currency: [{}]", e.getMessage(), e);
            return false;
        }
    }
}