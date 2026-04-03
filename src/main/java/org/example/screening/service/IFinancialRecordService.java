package org.example.screening.service;

import org.example.screening.dto.RecordRequest;
import org.example.screening.dto.RecordResponse;
import org.example.screening.entity.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IFinancialRecordService {
    RecordResponse createRecord(RecordRequest request,Long userId);
    Page<RecordResponse> getAllRecords(AuthUser authUser, Pageable pageable);
    RecordResponse updateRecord(RecordRequest recordRequest,Long recordId);
    void deleteRecord(Long recordId);
}
