package org.example.screening.dto;

public record LoginResponse(String status , String jwtToken, String refreshToken) {
}
