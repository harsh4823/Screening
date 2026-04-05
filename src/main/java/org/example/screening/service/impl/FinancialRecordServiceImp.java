package org.example.screening.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.screening.dto.RecordRequest;
import org.example.screening.dto.RecordResponse;
import org.example.screening.entity.AuthUser;
import org.example.screening.entity.FinancialRecord;
import org.example.screening.entity.Role;
import org.example.screening.entity.TransactionType;
import org.example.screening.exception.ResourceNotFoundException;
import org.example.screening.repository.AuthUserRepository;
import org.example.screening.repository.FinancialRecordRepository;
import org.example.screening.service.IFinancialRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialRecordServiceImp implements IFinancialRecordService {

    private final AuthUserRepository authUserRepository;
    private final FinancialRecordRepository recordRepository;

    @Transactional
    @Override
    public RecordResponse createRecord(RecordRequest request, Long userId) {
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User","UserId",userId.toString()));

        FinancialRecord financialRecord = FinancialRecord.builder()
                .amount(request.amount())
                .transactionType(request.type())
                .category(request.category())
                .description(request.description())
                .transactionDate(request.transactionDate())
                .user(user)
                .build();

        FinancialRecord saved = recordRepository.save(financialRecord);
        return buildResponse(saved);
    }

    @Override
    public Page<RecordResponse> getAllRecords(
            AuthUser authUser, Pageable pageable, TransactionType type,
            String category, LocalDate from, LocalDate to
    ) {
        Long userIdFilter = (authUser.getRole() == Role.ROLE_ADMIN || authUser.getRole() == Role.ROLE_ANALYST)
                ? null : authUser.getUserId();
        return recordRepository.findWithFilters(userIdFilter,type,category,from,to,pageable)
                .map(this::buildResponse);
    }

    @Transactional
    @Override
    public RecordResponse updateRecord(RecordRequest request, Long recordId) {
        FinancialRecord existingRecord = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record","RecordId",recordId.toString()));
        if (request.amount() != null) existingRecord.setAmount(request.amount());
        if (request.type() != null) existingRecord.setTransactionType(request.type());
        if (request.category() != null) existingRecord.setCategory(request.category());
        if (request.description() != null) existingRecord.setDescription(request.description());

        FinancialRecord saved = recordRepository.save(existingRecord);
        return buildResponse(saved);
    }

    @Transactional
    @Override
    public void deleteRecord(Long recordId) {
        FinancialRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record","RecordId",recordId.toString()));
        record.setDeleted(true);
        record.setDeletedAt(LocalDateTime.now());
        recordRepository.save(record);
    }

    private RecordResponse buildResponse(FinancialRecord saved) {
        return new RecordResponse(
                saved.getRecordId(),
                saved.getAmount(),
                saved.getTransactionType(),
                saved.getCategory(),
                saved.getDescription(),
                saved.getTransactionDate(),
                saved.getUser().getEmail()
        );
    }

}
