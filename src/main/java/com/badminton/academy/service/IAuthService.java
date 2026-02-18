package com.badminton.academy.service;

import com.badminton.academy.dto.request.LoginRequest;
import com.badminton.academy.dto.request.RegisterRequest;
import com.badminton.academy.dto.request.RefreshTokenRequest;
import com.badminton.academy.dto.response.AuthResponse;

public interface IAuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String token);
}
