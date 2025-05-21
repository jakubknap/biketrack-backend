package pl.biketrack.user.service;

import pl.biketrack.user.model.User;

public interface UserService {

    User getUserByEmail(String email);
}