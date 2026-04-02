package org.example.screening.repository;

import org.example.screening.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByAuthUser_UserId(Long userId);

    Optional<RefreshToken> findRefreshTokenByToken(String token);
}