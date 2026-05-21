package com.shadowspend.repository;

import com.shadowspend.model.SubscriptionTransaction;
import com.shadowspend.model.SubscriptionTransactionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionTransactionRepository extends JpaRepository<SubscriptionTransaction, SubscriptionTransactionId> {
}
