package pl.biketrack.base.exception;

import lombok.Getter;
import pl.biketrack.base.enumerated.ResponseCode;

@Getter
public class ServiceException extends RuntimeException {

    private final ResponseCode status;

    public ServiceException(ResponseCode status) {
        super(status.getMessage());
        this.status = status;
    }
}