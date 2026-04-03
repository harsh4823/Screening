package org.example.screening.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.screening.entity.FinancialRecord;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    Page<FinancialRecord> findAll(@NonNull Pageable pageable);

    @Query("SELECT f FROM FinancialRecord f where f.user.userId = :userId")
    Page<FinancialRecord> findByUserId(@Param("userId") Long userId, Pageable pageable);
}