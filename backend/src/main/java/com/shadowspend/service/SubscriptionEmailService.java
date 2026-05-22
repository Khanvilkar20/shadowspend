package com.shadowspend.service;

import com.shadowspend.dto.SubscriptionEmailDTO;
import com.shadowspend.model.Email;
import com.shadowspend.model.Subscription;
import com.shadowspend.model.SubscriptionTransaction;
import com.shadowspend.model.Transaction;
import com.shadowspend.repository.SubscriptionRepository;
import com.shadowspend.repository.SubscriptionTransactionRepository;
import com.shadowspend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionEmailService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionTransactionRepository subscriptionTransactionRepository;
    private final TransactionRepository transactionRepository;

    public List<SubscriptionEmailDTO> getEmailsForSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));

        List<SubscriptionTransaction> links =
                subscriptionTransactionRepository.findById_SubscriptionId(subscription.getId());
        if (links.isEmpty()) {
            return List.of();
        }

        List<UUID> transactionIds = links.stream()
                .map(link -> link.getId().getTransactionId())
                .toList();

        List<Transaction> transactions = transactionRepository.findByIdInWithEmail(transactionIds);
        Map<UUID, SubscriptionEmailDTO> byEmailId = new LinkedHashMap<>();

        for (Transaction tx : transactions) {
            Email email = tx.getEmail();
            if (email == null || byEmailId.containsKey(email.getId())) {
                continue;
            }
            byEmailId.put(email.getId(), toDTO(email, tx.getAmount()));
        }

        return new ArrayList<>(byEmailId.values());
    }

    private SubscriptionEmailDTO toDTO(Email email, BigDecimal amount) {
        double amountValue = amount != null ? amount.doubleValue() : 0.0;
        return SubscriptionEmailDTO.builder()
                .id(email.getId())
                .subject(email.getSubject())
                .sender(email.getSender())
                .snippet(email.getSnippet())
                .rawBody(email.getRawBody())
                .receivedAt(email.getReceivedAt())
                .amount(amountValue)
                .build();
    }
}
