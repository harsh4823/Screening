package org.example.screening.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.screening.entity.FinancialRecord;
import org.example.screening.entity.TransactionType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    Page<FinancialRecord> findAll(@NonNull Pageable pageable);

    @Query("""
        SELECT f FROM FinancialRecord f
        WHERE (:userId IS NULL OR f.user.userId = :userId)
        AND (:type IS NULL OR f.transactionType = :type)
        AND (:category IS NULL OR LOWER(f.category) = LOWER(:category))
        AND (:from IS NULL OR f.createdAt >= :from)
        AND (:to IS NULL OR f.createdAt <= :to)
    """)
    Page<FinancialRecord> findWithFilters(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}