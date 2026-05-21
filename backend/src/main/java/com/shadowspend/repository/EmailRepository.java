package com.shadowspend.repository;

import com.shadowspend.model.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmailRepository extends JpaRepository<Email, UUID> {
    List<Email> findByUserIdAndProcessedFalse(UUID userId);
    boolean existsByGmailId(String gmailId);
    List<Email> findByUserId(UUID userId);
}
