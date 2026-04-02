package org.example.screening.service;


import org.example.screening.dto.AuthUserDto;
import org.example.screening.dto.LoginRequest;
import org.example.screening.dto.LoginResponse;
import org.example.screening.entity.AuthUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IAuthService {

    LoginResponse authenticateAndGenerateToken(LoginRequest loginRequest);

    void registerUser(AuthUserDto authUserDto) throws IOException;

    void logoutFromSingleDevice(Long userId,String jwt,String refreshToken);

    void logoutFromAllDevices(Long userId);
}
