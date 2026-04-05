package org.example.screening.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.screening.dto.AuthUserResponse;
import org.example.screening.entity.Role;
import org.example.screening.service.IAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;

    @PatchMapping("/update/role/{userId}")
    public ResponseEntity<AuthUserResponse> updateUserRole(
            @PathVariable Long userId,
            @RequestParam Role role
    ){
        AuthUserResponse response = adminService.updateRole(userId,role);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/toogle/status/{userId}")
    public ResponseEntity<String> toogleStatus(@PathVariable Long userId){
        return ResponseEntity.ok(adminService.toggleStatus(userId));
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId){
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get")
    public ResponseEntity<Page<AuthUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(adminService.getAllUsers(PageRequest.of(page,size)));
    }
}
