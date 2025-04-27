package pl.biketrack.base.controller.advice;

import lombok.Getter;
import pl.biketrack.base.enumerated.ResponseCode;

@Getter
public class BaseValidationResponse<T> extends BaseResponse {

    private T errors;

    public BaseValidationResponse(ResponseCode status, T errors) {
        super(status);
        this.errors = errors;
    }

    public BaseValidationResponse(ResponseCode status, String message) {
        super(status, message);
    }
}