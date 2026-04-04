package org.example.screening.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryDto(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        List<CategoryTotal> categoryTotals,
        List<WeeklyTrends> weeklyTrends,
        List<MonthlyTrends> monthlyTrends
)
{}
