package pl.biketrack.common.enumerated;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {

    // GLOBAL SUCCESSES
    S00000(Constants.SUCCESS, OK),
    S00001(Constants.SUCCESS, NO_CONTENT),

    // GLOBAL ERRORS
    E00000("Bad Request", BAD_REQUEST),
    E00001("Forbidden", FORBIDDEN),
    E00002("Not found", NOT_FOUND),
    E00003("Unauthorized", UNAUTHORIZED),
    E00004(Constants.INVALID_FORMAT_JSON, UNPROCESSABLE_ENTITY),
    E00005(Constants.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR),

    // USER ERRORS
    E01000("User not found", NOT_FOUND),
    E01001("User already exists", CONFLICT),
    E01002("No authenticated user found", INTERNAL_SERVER_ERROR),
    E01003("Principal is not instance of User", INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;

    private static class Constants {
        public static final String SUCCESS = "Success";
        public static final String INVALID_FORMAT_JSON = "Invalid format JSON";
        public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    }
}