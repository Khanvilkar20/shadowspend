package com.shadowspend.repository;

import com.shadowspend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserId(UUID userId);
    List<Transaction> findByUserIdAndNormalizedMerchantName(UUID userId, String name);
    boolean existsByEmailId(UUID emailId);
}
