package org.example.screening.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.screening.dto.AuthUserResponse;
import org.example.screening.entity.AuthUser;
import org.example.screening.entity.Role;
import org.example.screening.exception.ResourceNotFoundException;
import org.example.screening.repository.AuthUserRepository;
import org.example.screening.repository.FinancialRecordRepository;
import org.example.screening.repository.RefreshTokenRepository;
import org.example.screening.repository.TokenRepository;
import org.example.screening.service.IAdminService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminServiceImp implements IAdminService {

    private final AuthUserRepository authUserRepository;
    private final FinancialRecordRepository recordRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenRepository tokenRepository;

    @Override
    public AuthUserResponse updateRole(Long userId, Role role) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User","UserId",userId.toString()));
        user.setRole(role);
        authUserRepository.save(user);
        return new AuthUserResponse(user.getEmail(),user.getName(),user.getRole().name());
    }

    @Override
    public String toggleStatus(Long userId){
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User","UserId",userId.toString()));
        user.setActive(!user.isActive());
        authUserRepository.save(user);
        return user.isActive() ? "ACTIVE" : "INACTIVE";
    }

    @Override
    public void deleteUser(Long userId){
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User","UserId",userId.toString()));
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setActive(false);
        authUserRepository.save(user);

        tokenRepository.removeAllTokens(userId);
        refreshTokenRepository.deleteByAuthUser_UserId(userId);
    }
}
