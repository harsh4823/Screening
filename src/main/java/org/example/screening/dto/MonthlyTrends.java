package org.example.screening.dto;

import java.math.BigDecimal;

public record MonthlyTrends(int year, int month, BigDecimal income,BigDecimal expense) {
}
