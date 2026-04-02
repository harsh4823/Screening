package org.example.screening.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.screening.constant.ApplicationConstant;
import org.example.screening.dto.AuthUserDto;
import org.example.screening.dto.LoginRequest;
import org.example.screening.dto.LoginResponse;
import org.example.screening.entity.AuthUser;
import org.example.screening.entity.RefreshToken;
import org.example.screening.repository.AuthUserRepository;
import org.example.screening.repository.TokenRepository;
import org.example.screening.service.IAuthService;
import org.example.screening.service.IRefreshTokenService;
import org.example.screening.util.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthUserController {

    private final IAuthService authService;
    private final IRefreshTokenService refreshTokenService;
    private final AuthUtil authUtil;
    private final TokenRepository tokenRepository;
    private final AuthUserRepository authUserRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> apiLogin(@RequestBody LoginRequest loginRequest){
        try{
        LoginResponse loginResponse = authService.authenticateAndGenerateToken(loginRequest);

        return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstant.JWT_HEADER,loginResponse.jwtToken())
                .body(loginResponse);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping(value = "/register")
    public ResponseEntity<?> register(@RequestBody AuthUserDto authUserDto){
        try{
            authService.registerUser(authUserDto);
            return ResponseEntity.status(HttpStatus.CREATED).
                        body("Given user details are successfully registered");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("An exception occurred: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenString = authUtil.extractRefreshToken(request);

        if (refreshTokenString == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Refresh Token is missing!");
        }

        if (tokenRepository.isRefreshTokenBlacklisted(refreshTokenString)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Refresh Token hs been revoked!");
        }

        return refreshTokenService.findByToken(refreshTokenString)
                .map(refreshTokenService::verifyRefreshToken)
                .map(RefreshToken::getAuthUser)
                .map(authUser -> {

                    String newAccessToken = authUtil.generateJWTToken(authUser);

                    tokenRepository.storeTokens(authUser.getUserId(),newAccessToken,refreshTokenString);

                    return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstant.JWT_HEADER,newAccessToken)
                            .body(new LoginResponse(HttpStatus.OK.getReasonPhrase(),newAccessToken,refreshTokenString));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @PostMapping("/logout/single")
    public ResponseEntity<?> logoutFromSingleDevice(HttpServletRequest request, HttpServletResponse response) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AuthUser authUser = authUserRepository.findByEmail(email).orElse(null);

        if (authUser != null) {
            String jwt = authUtil.extractJwt(request);
            String refreshToken = authUtil.extractRefreshToken(request);

            authService.logoutFromSingleDevice(authUser.getUserId(), jwt, refreshToken);

            authUtil.clearBrowserCookies(response);
            return ResponseEntity.status(HttpStatus.OK).body("Logout successful");
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Logout failed");
    }

    @PostMapping("/logout/all")
    public ResponseEntity<?> logoutFromAllDevices(HttpServletResponse response) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AuthUser authUser = authUserRepository.findByEmail(email).orElse(null);

        if (authUser != null) {
            authService.logoutFromAllDevices(authUser.getUserId());
            authUtil.clearBrowserCookies(response);
            return ResponseEntity.status(HttpStatus.OK).body("Logout successful from all devices");
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Logout failed");
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }
}
