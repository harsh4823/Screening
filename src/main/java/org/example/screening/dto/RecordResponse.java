package org.example.screening.dto;

import org.example.screening.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecordResponse(
    Long recordId,
    BigDecimal amount,
    TransactionType type,
    String category,
    String description,
    LocalDateTime date,
    String createdBy
)
{}
