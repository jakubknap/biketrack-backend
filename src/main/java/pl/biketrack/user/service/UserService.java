package pl.biketrack.user.service;

import pl.biketrack.exception.dto.response.BaseResponse;
import pl.biketrack.user.dto.request.ChangePasswordRequest;
import pl.biketrack.user.dto.request.UpdateUserRequest;
import pl.biketrack.user.model.User;

public interface UserService {

    User getUserByEmail(String email);

    BaseResponse updateUser(UpdateUserRequest request);

    BaseResponse changePassword(ChangePasswordRequest request);
}