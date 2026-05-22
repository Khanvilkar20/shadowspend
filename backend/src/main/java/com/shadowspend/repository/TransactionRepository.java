package com.shadowspend.repository;

import com.shadowspend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserId(UUID userId);
    List<Transaction> findByUserIdAndNormalizedMerchantName(UUID userId, String name);
    boolean existsByEmailId(UUID emailId);

    @Query("""
            SELECT t FROM Transaction t
            JOIN FETCH t.email e
            WHERE t.id IN :transactionIds
            ORDER BY e.receivedAt DESC
            """)
    List<Transaction> findByIdInWithEmail(@Param("transactionIds") List<UUID> transactionIds);
}
