package org.example.screening.util;

import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.screening.entity.AuthUser;
import org.example.screening.exception.ResourceNotFoundException;
import org.example.screening.repository.AuthUserRepository;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final Environment env;
    private final AuthUserRepository authUserRepository;
    private PrivateKey privateKey;
    @Getter
    private PublicKey publicKey;

    @PostConstruct
    public void initKeys() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        }catch (Exception e) {
            throw new RuntimeException("Failed to initialize RSA key pair", e);
        }
    }

    public String generateJWTToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        if (authorities.isEmpty()) {
            authorities = "ROLE_VIEWER";
        }

        return Jwts.builder()
                .issuer("Harsh")
                .subject("JWT Token")
                .claim("email", authentication.getName())
                .claim("authorities", authorities)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generateJWTToken(AuthUser authUser){
        return Jwts.builder()
                .issuer("Harsh")
                .subject("JWT Token")
                .claim("email", authUser.getEmail())
                .claim("authorities", authUser.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String extractJwt(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } else if (bearerToken != null) {
            return bearerToken;
        }
        return extractCookie(request, "jwt");
    }

    public String extractRefreshToken(HttpServletRequest request) {
        String refreshHeader = request.getHeader("Refresh-Token");
        if (refreshHeader != null) {
            return refreshHeader;
        }
        return extractCookie(request, "refreshToken");
    }

    public String extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public void clearBrowserCookies(HttpServletResponse response) {
        Cookie clearJwtCookie = new Cookie("jwt", null);
        clearJwtCookie.setPath("/");
        clearJwtCookie.setHttpOnly(true);
        clearJwtCookie.setMaxAge(0);

        Cookie clearRefreshCookie = new Cookie("refreshToken", null);
        clearRefreshCookie.setPath("/api/auth/refresh");
        clearRefreshCookie.setHttpOnly(true);
        clearRefreshCookie.setMaxAge(0);

        response.addCookie(clearJwtCookie);
        response.addCookie(clearRefreshCookie);
    }

    public AuthUser resolveUser(Authentication authentication){
        return authUserRepository.findByEmail((String) authentication.getPrincipal())
                .orElseThrow(() -> new ResourceNotFoundException("User","Email",authentication.getName()));
    }

}
