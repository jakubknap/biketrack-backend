package pl.biketrack.authentication.service;

import pl.biketrack.authentication.dto.request.ConfirmResetPasswordRequest;
import pl.biketrack.authentication.dto.request.ResetPasswordRequest;
import pl.biketrack.exception.dto.response.BaseResponse;

public interface PasswordService {

    BaseResponse resetPasswordRequest(ResetPasswordRequest request);

    BaseResponse confirmResetPassword(ConfirmResetPasswordRequest request);
}