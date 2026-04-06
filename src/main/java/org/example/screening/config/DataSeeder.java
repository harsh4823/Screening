package org.example.screening.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.screening.entity.AuthUser;
import org.example.screening.entity.Role;
import org.example.screening.repository.AuthUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@finance.com";

        if (authUserRepository.findByEmail(adminEmail).isEmpty()) {
            AuthUser admin = AuthUser.builder()
                    .name("Super Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ROLE_ADMIN)
                    .active(true)
                    .build();

            authUserRepository.save(admin);
            log.info("Default admin created → email: {}, password: Admin@123",adminEmail);
        } else {
            log.info("Admin already exists, skipping seed.");
        }
    }
}