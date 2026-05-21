package com.shadowspend.repository;

import com.shadowspend.model.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ScanHistoryRepository extends JpaRepository<ScanHistory, UUID> {
    Optional<ScanHistory> findTopByUserIdOrderByStartedAtDesc(UUID userId);
    boolean existsByUserIdAndStatus(UUID userId, String status);
    long countByUserIdAndStatus(UUID userId, String status);
}
