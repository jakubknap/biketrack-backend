package pl.biketrack.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pl.biketrack.exception.dto.BaseApiValidationError;
import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.exception.dto.response.BaseValidationResponse;
import pl.biketrack.exception.exception.CustomValidationException;
import pl.biketrack.exception.exception.ServiceException;

import java.util.List;

import static pl.biketrack.common.enumerated.ResponseCode.E00000;
import static pl.biketrack.common.enumerated.ResponseCode.E00004;
import static pl.biketrack.common.enumerated.ResponseCode.E00005;
import static pl.biketrack.common.enumerated.ResponseCode.E00006;
import static pl.biketrack.exception.mapper.BaseValidationErrorMapper.mapBindingResult;
import static pl.biketrack.exception.mapper.BaseValidationErrorMapper.mapCustomValidationErrors;

@Slf4j
@RestControllerAdvice
public class BaseControllerAdvice {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse> handleException(HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException error: {}", ex.getMessage(), ex);
        return new BaseResponse(E00004).get();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> handleException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException error: {}", ex.getMessage(), ex);
        List<BaseApiValidationError> errors = mapBindingResult(ex.getBindingResult());
        BaseValidationResponse<List<BaseApiValidationError>> validationResponse = new BaseValidationResponse<>(E00000, errors);
        return validationResponse.get();
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<BaseResponse> handleException(MissingPathVariableException ex) {
        log.error("MissingPathVariableException error: {}", ex.getMessage(), ex);
        return new BaseResponse(E00005).get();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse> handleException(HttpRequestMethodNotSupportedException ex) {
        log.error("HttpRequestMethodNotSupportedException error: {}", ex.getMessage(), ex);
        BaseValidationResponse<String> validationResponse = new BaseValidationResponse<>(E00000, "Unsupported request method");
        return validationResponse.get();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse> handleException(MethodArgumentTypeMismatchException ex) {
        log.error("MethodArgumentTypeMismatchException error: {}", ex.getMessage(), ex);
        return new BaseResponse(E00005).get();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse> handleException(NoResourceFoundException ex) {
        log.error("NoResourceFoundException error: {}", ex.getMessage(), ex);
        return new BaseResponse(E00005).get();
    }

    @ExceptionHandler(CustomValidationException.class)
    public ResponseEntity<BaseResponse> handleException(CustomValidationException ex) {
        log.error("CustomValidationException error: {}", ex.getMessage(), ex);
        List<BaseApiValidationError> errors = mapCustomValidationErrors(ex.getErrors());
        BaseValidationResponse<List<BaseApiValidationError>> validationResponse = new BaseValidationResponse<>(E00000, errors);
        return validationResponse.get();
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<BaseResponse> handleException(ServiceException ex) {
        log.error("Service Exception: {}", ex.getMessage(), ex);
        return new BaseResponse(ex.getStatus()).get();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse> handleException(Exception ex) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        return new BaseResponse(E00006).get();
    }
}