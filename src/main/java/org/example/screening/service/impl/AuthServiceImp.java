package org.example.screening.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.screening.dto.AuthUserDto;
import org.example.screening.dto.LoginRequest;
import org.example.screening.dto.LoginResponse;
import org.example.screening.entity.AuthUser;
import org.example.screening.entity.Role;
import org.example.screening.exception.ResourceNotFoundException;
import org.example.screening.repository.AuthUserRepository;
import org.example.screening.repository.TokenRepository;
import org.example.screening.service.IAuthService;
import org.example.screening.service.IRefreshTokenService;
import org.example.screening.util.AuthUtil;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final Environment env;
    private final PasswordEncoder passwordEncoder;
    private final AuthUserRepository authUserRepository;
    private final AuthUtil authUtil;
    private final IRefreshTokenService refreshTokenService;
    private final TokenRepository tokenRepository;

    @Override
    public LoginResponse authenticateAndGenerateToken(LoginRequest loginRequest) {
        Authentication authentication = UsernamePasswordAuthenticationToken
                .unauthenticated(loginRequest.email(), loginRequest.password());
        Authentication authenticate = authenticationManager.authenticate(authentication);

        if (authenticate != null && authenticate.isAuthenticated()) {
            AuthUser authUser = authUserRepository.findByEmail(loginRequest.email())
                    .orElseThrow(() -> new ResourceNotFoundException("User","email",loginRequest.email()));

            String jwt = authUtil.generateJWTToken(authenticate);
            String refreshToken = refreshTokenService.createRefreshToken(authUser.getUserId()).getToken();
            tokenRepository.storeTokens(authUser.getUserId(), jwt, refreshToken);

            return new LoginResponse(HttpStatus.OK.getReasonPhrase(), jwt, refreshToken);
        }
        throw new BadCredentialsException("Authentication failed");
    }

    @Transactional
    @Override
    public void registerUser(AuthUserDto authUserDto) throws IOException {
        Role userRole = Role.ROLE_VIEWER;

        String hashPassword = passwordEncoder.encode(authUserDto.password());
            AuthUser authUser = AuthUser.builder()
                    .name(authUserDto.name())
                    .password(hashPassword)
                    .email(authUserDto.email())
                    .role(userRole)
                    .build();

            authUserRepository.save(authUser);

    }

    @Override
    public void logoutFromAllDevices(Long userId){
        tokenRepository.removeAllTokens(userId);
    }

    @Override
    public void logoutFromSingleDevice(Long userId,String jwt,String refreshToken){
        tokenRepository.removeSingleSession(userId,jwt,refreshToken);
    }
}
