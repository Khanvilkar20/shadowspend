package com.shadowspend.repository;

import com.shadowspend.model.SubscriptionTransaction;
import com.shadowspend.model.SubscriptionTransactionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionTransactionRepository extends JpaRepository<SubscriptionTransaction, SubscriptionTransactionId> {
    List<SubscriptionTransaction> findById_SubscriptionId(UUID subscriptionId);
}
