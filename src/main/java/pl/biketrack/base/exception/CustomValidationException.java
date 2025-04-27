package pl.biketrack.base.exception;

import lombok.Getter;
import pl.biketrack.base.controller.advice.BaseApiValidationError;
import pl.biketrack.base.enumerated.ResponseCode;

import java.util.List;

@Getter
public class CustomValidationException extends RuntimeException {

    private final ResponseCode status;
    private final List<BaseApiValidationError> errors;

    public CustomValidationException(ResponseCode status, List<BaseApiValidationError> errors) {
        super(status.getMessage());
        this.status = status;
        this.errors = errors;
    }
}