package org.example.screening.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.screening.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordRequest(
        @Positive(message = "Amount should be positive") BigDecimal amount,
        @NotNull TransactionType type,
        @NotBlank String category,
        String description,
        LocalDate transactionDate
)
{}
