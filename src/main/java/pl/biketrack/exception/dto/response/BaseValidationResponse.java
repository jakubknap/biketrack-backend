package pl.biketrack.exception.dto.response;

import lombok.Getter;
import pl.biketrack.common.enumerated.ResponseCode;

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