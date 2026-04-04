package org.example.screening.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.screening.dto.CategoryTotal;
import org.example.screening.dto.DashboardSummaryDto;
import org.example.screening.dto.MonthlyTrends;
import org.example.screening.dto.WeeklyTrends;
import org.example.screening.repository.FinancialRecordRepository;
import org.example.screening.service.IDashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImp implements IDashboardService {

    private final FinancialRecordRepository recordRepository;

    @Override
    public DashboardSummaryDto getDashboardSummary() {
        BigDecimal totalIncome = Optional.ofNullable(recordRepository.getTotalIncome())
                .orElse(BigDecimal.ZERO);
        BigDecimal totalExpense = Optional.ofNullable(recordRepository.getTotalExpense())
                .orElse(BigDecimal.ZERO);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        List<CategoryTotal> categoryTotals = recordRepository.getCategoryTotals().stream()
                .map(row -> new CategoryTotal(
                        (String) row[0],
                        (BigDecimal) row[1]
                )).toList();

        List<WeeklyTrends> weeklyTrends = recordRepository.getWeeklyTrends().stream()
                .map(row -> new WeeklyTrends(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        (BigDecimal) row[2],
                        (BigDecimal) row[3]
                )).toList();

        List<MonthlyTrends> monthlyTrends = recordRepository.getMonthlyTotals().stream()
                .map(row -> new MonthlyTrends(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        (BigDecimal) row[2],
                        (BigDecimal) row[3]
                )).toList();

        return new DashboardSummaryDto(
                totalIncome,
                totalExpense,
                netBalance,
                categoryTotals,
                weeklyTrends,
                monthlyTrends
        );
    }
}
