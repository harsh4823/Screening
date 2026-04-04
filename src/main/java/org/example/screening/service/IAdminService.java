package org.example.screening.service;

import org.example.screening.dto.AuthUserResponse;
import org.example.screening.entity.Role;

public interface IAdminService {
    AuthUserResponse updateRole(Long userId, Role role);
    String toggleStatus(Long userId);
    void deleteUser(Long userId);
}
