package org.example.screening.service;


import org.example.screening.entity.RefreshToken;

import java.util.Optional;

public interface IRefreshTokenService {
    RefreshToken createRefreshToken(Long userId);
    RefreshToken verifyRefreshToken(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
}
