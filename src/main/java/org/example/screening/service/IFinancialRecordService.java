package org.example.screening.service;

import org.example.screening.dto.RecordRequest;
import org.example.screening.dto.RecordResponse;
import org.example.screening.entity.AuthUser;
import org.example.screening.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface IFinancialRecordService {
    RecordResponse createRecord(RecordRequest request,Long userId);
    Page<RecordResponse> getAllRecords(
            AuthUser authUser,
            Pageable pageable,
            TransactionType type,
            String category,
            LocalDate from,
            LocalDate to
    );
    RecordResponse updateRecord(RecordRequest recordRequest,Long recordId);
    void deleteRecord(Long recordId);
}
