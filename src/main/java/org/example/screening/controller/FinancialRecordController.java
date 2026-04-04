package org.example.screening.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.screening.dto.RecordRequest;
import org.example.screening.dto.RecordResponse;
import org.example.screening.entity.AuthUser;
import org.example.screening.entity.TransactionType;
import org.example.screening.service.IFinancialRecordService;
import org.example.screening.util.AuthUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final IFinancialRecordService financialRecordService;
    private final AuthUtil authUtil;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecordResponse> createRecord(
            @Valid @RequestBody RecordRequest request,
            Authentication authentication
            ){
        AuthUser authUser = authUtil.resolveUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(financialRecordService.createRecord(request,authUser.getUserId()));
    }

    @GetMapping("/get")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    public ResponseEntity<Page<RecordResponse>> getAllRecords(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ){
        AuthUser authUser = authUtil.resolveUser(authentication);
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atStartOfDay() : null;

        return ResponseEntity.ok(financialRecordService.getAllRecords(
                authUser,pageable,type,category, fromDateTime, toDateTime));
    }

    @PutMapping("/update/{recordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RecordResponse> updateRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody RecordRequest request
    ){
        return ResponseEntity.ok(financialRecordService.updateRecord(request,recordId));
    }

    @DeleteMapping("/delete/{recordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long recordId){
        financialRecordService.deleteRecord(recordId);
        return ResponseEntity.noContent().build();
    }
}
