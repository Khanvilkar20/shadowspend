package com.shadowspend.controller;

import com.shadowspend.dto.ScanRequest;
import com.shadowspend.dto.ScanStatusDTO;
import com.shadowspend.model.ScanHistory;
import com.shadowspend.model.User;
import com.shadowspend.repository.ScanHistoryRepository;
import com.shadowspend.repository.UserRepository;
import com.shadowspend.service.ScanOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class ScanController {

    private final ScanHistoryRepository scanHistoryRepository;
    private final UserRepository userRepository;
    private final ScanOrchestrator scanOrchestrator;

    @PostMapping
    public ResponseEntity<Map<String, UUID>> scan(@Valid @RequestBody ScanRequest request) {
        if (scanHistoryRepository.existsByUserIdAndStatus(request.getUserId(), "running")) {
            UUID runningId = scanHistoryRepository.findTopByUserIdOrderByStartedAtDesc(request.getUserId())
                    .map(ScanHistory::getId).orElse(null);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(runningId == null ? Map.of() : Map.of("scanId", runningId));
        }
        User user = userRepository.findById(request.getUserId()).orElseThrow();
        ScanHistory history = new ScanHistory();
        history.setUser(user);
        history.setStartedAt(OffsetDateTime.now(ZoneOffset.UTC));
        history.setStatus("running");
        history.setEmailsFetched(0);
        history.setEmailsParsed(0);
        history.setSubscriptionsFound(0);
        history = scanHistoryRepository.save(history);
        scanOrchestrator.runScan(request.getUserId(), history.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("scanId", history.getId()));
    }

    @GetMapping("/status/{scanId}")
    public ScanStatusDTO status(@PathVariable UUID scanId) {
        ScanHistory history = scanHistoryRepository.findById(scanId).orElseThrow();
        return ScanStatusDTO.builder()
                .scanId(history.getId())
                .status(history.getStatus())
                .emailsFetched(history.getEmailsFetched())
                .emailsParsed(history.getEmailsParsed())
                .subscriptionsFound(history.getSubscriptionsFound())
                .errorMessage(history.getErrorMessage())
                .build();
    }
}
