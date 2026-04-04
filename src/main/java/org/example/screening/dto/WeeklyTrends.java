package org.example.screening.dto;

import java.math.BigDecimal;

public record WeeklyTrends(int year, int week, BigDecimal income, BigDecimal expense) {
}
