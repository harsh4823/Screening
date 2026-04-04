package org.example.screening.repository;

import org.example.screening.entity.FinancialRecord;
import org.example.screening.entity.TransactionType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    Page<FinancialRecord> findAll(@NonNull Pageable pageable);

    @Query("""
        SELECT f FROM FinancialRecord f
        WHERE (:userId IS NULL OR f.user.userId = :userId)
        AND (:type IS NULL OR f.transactionType = :type)
        AND (:category IS NULL OR LOWER(f.category) = LOWER(:category))
        AND (:from IS NULL OR f.transactionDate >= :from)
        AND (:to IS NULL OR f.transactionDate <= :to)
    """)
    Page<FinancialRecord> findWithFilters(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    @Query("SELECT SUM(f.amount) FROM FinancialRecord f WHERE f.transactionType = 'INCOME'")
    BigDecimal getTotalIncome();

    @Query("SELECT SUM(f.amount) FROM FinancialRecord f WHERE f.transactionType = 'EXPENSE'")
    BigDecimal getTotalExpense();

    @Query("SELECT f.category, SUM(f.amount) FROM FinancialRecord f GROUP BY f.category")
    List<Object[]> getCategoryTotals();

    @Query("""
        SELECT
        FUNCTION('YEAR', f.createdAt),
        FUNCTION('MONTH', f.createdAt),
        SUM(CASE WHEN f.transactionType = 'INCOME'  THEN f.amount ELSE 0 END),
        SUM(CASE WHEN f.transactionType = 'EXPENSE' THEN f.amount ELSE 0 END)
        FROM FinancialRecord f
        GROUP BY FUNCTION('YEAR', f.createdAt), FUNCTION('MONTH', f.createdAt)
        ORDER BY 1 DESC, 2 DESC
""")
    List<Object[]> getMonthlyTotals();

    @Query("""
    SELECT 
        FUNCTION('YEAR', f.createdAt),
        FUNCTION('WEEK', f.createdAt),
        SUM(CASE WHEN f.transactionType = 'INCOME'  THEN f.amount ELSE 0 END),
        SUM(CASE WHEN f.transactionType = 'EXPENSE' THEN f.amount ELSE 0 END)
    FROM FinancialRecord f
    GROUP BY FUNCTION('YEAR', f.createdAt), FUNCTION('WEEK', f.createdAt)
    ORDER BY 1 DESC, 2 DESC
""")
    List<Object[]> getWeeklyTrends();

    @Modifying
    @Query("DELETE FROM FinancialRecord f WHERE f.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}