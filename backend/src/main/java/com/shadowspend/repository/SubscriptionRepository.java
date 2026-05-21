package com.shadowspend.repository;

import com.shadowspend.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUserId(UUID userId);
    List<Subscription> findByUserIdAndIsActiveTrue(UUID userId);
    List<Subscription> findByUserIdAndCategory(UUID userId, String category);
    List<Subscription> findByNextBillingDateBeforeAndIsActiveTrue(LocalDate date);
    Optional<Subscription> findByUserIdAndNormalizedServiceName(UUID userId, String name);
}
