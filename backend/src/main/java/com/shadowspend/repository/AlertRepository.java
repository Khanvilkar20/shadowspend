package com.shadowspend.repository;

import com.shadowspend.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByUserIdAndSentFalseAndDismissedFalse(UUID userId);
    List<Alert> findByScheduledAtBeforeAndSentFalse(OffsetDateTime time);
    boolean existsBySubscriptionIdAndScheduledAtAfter(UUID subscriptionId, OffsetDateTime time);
}
