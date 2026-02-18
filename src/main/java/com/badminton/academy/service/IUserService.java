package com.badminton.academy.service;

import com.badminton.academy.dto.request.UpdateUserRequest;
import com.badminton.academy.dto.response.UserResponse;
import java.util.List;

public interface IUserService {
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
    void activateUser(Long id);
    void deactivateUser(Long id);
}
