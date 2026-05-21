// package com.shadowspend.service;

// import com.shadowspend.model.Email;
// import com.shadowspend.model.ScanHistory;
// import com.shadowspend.model.Subscription;
// import com.shadowspend.model.SubscriptionTransaction;
// import com.shadowspend.model.SubscriptionTransactionId;
// import com.shadowspend.model.Transaction;
// import com.shadowspend.model.User;
// import com.shadowspend.repository.EmailRepository;
// import com.shadowspend.repository.ScanHistoryRepository;
// import com.shadowspend.repository.SubscriptionRepository;
// import com.shadowspend.repository.SubscriptionTransactionRepository;
// import com.shadowspend.repository.TransactionRepository;
// import com.shadowspend.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;

// import java.time.OffsetDateTime;
// import java.time.ZoneOffset;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.UUID;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class ScanOrchestrator {

//     private final ScanHistoryRepository scanHistoryRepository;
//     private final UserRepository userRepository;
//     private final GmailService gmailService;
//     private final EmailRepository emailRepository;
//     private final EmailParserService emailParserService;
//     private final TransactionRepository transactionRepository;
//     private final DetectionService detectionService;
//     private final SubscriptionRepository subscriptionRepository;
//     private final SubscriptionTransactionRepository subscriptionTransactionRepository;

//     @Async
//     public void runScan(UUID userId, UUID scanId) {
//         log.info("runScan called for user {} and scan {}", userId, scanId);
//         ScanHistory history = scanHistoryRepository.findById(scanId).orElse(null);
//         if (history == null) {
//             return;
//         }
//         try {
//             if (scanHistoryRepository.countByUserIdAndStatus(userId, "running") > 1) {
//                 history.setStatus("skipped");
//                 history.setErrorMessage("Another scan is already running");
//                 history.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                 scanHistoryRepository.save(history);
//                 return;
//             }
//             history.setStatus("running");
//             scanHistoryRepository.save(history);
//             User user = userRepository.findById(userId).orElseThrow();
//             user = gmailService.refreshTokenIfNeeded(user);
//             int fetched = gmailService.fetchEmails(user);
//             history.setEmailsFetched(fetched);
//             List<Email> unprocessed = emailRepository.findByUserIdAndProcessedFalse(userId);
//             int parsedCount = 0;
//             for (Email email : unprocessed) {
//                 try {
//                     if (Boolean.TRUE.equals(email.getProcessed())) {
//                         continue;
//                     }
//                     Transaction parsed = emailParserService.parseEmail(email);
//                     if (parsed != null && !transactionRepository.existsByEmailId(email.getId())) {
//                         transactionRepository.save(parsed);
//                         parsedCount++;
//                     }
//                     email.setProcessed(Boolean.TRUE);
//                     emailRepository.save(email);
//                 } catch (Exception ex) {
//                     log.error("Error while parsing email {}", email.getId(), ex);
//                     email.setProcessingError(ex.getMessage());
//                     email.setProcessed(Boolean.TRUE);
//                     emailRepository.save(email);
//                 }
//             }
//             history.setEmailsParsed(parsedCount);

//             List<Transaction> transactions = transactionRepository.findByUserId(userId);
//             Map<String, List<Transaction>> grouped = detectionService.groupTransactionsByMerchant(transactions);
//             int subscriptionsFound = 0;
//             for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {
//                 String merchant = entry.getKey();
//                 List<Transaction> merchantTransactions = entry.getValue();
//                 List<Email> emails = new ArrayList<>();
//                 for (Transaction tx : merchantTransactions) {
//                     if (tx.getEmail() != null) {
//                         emails.add(tx.getEmail());
//                     }
//                 }
//                 Subscription detected = detectionService.buildSubscription(merchant, merchantTransactions, emails);
//                 Subscription subscription = subscriptionRepository.findByUserIdAndNormalizedServiceName(userId, merchant).orElse(null);
//                 if (subscription != null) {
//                     subscription.setAmount(detected.getAmount());
//                     subscription.setOccurrenceCount(detected.getOccurrenceCount());
//                     subscription.setNextBillingDate(detected.getNextBillingDate());
//                     subscription.setConfidenceScore(detected.getConfidenceScore());
//                     subscription.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                 } else {
//                     subscription = detected;
//                     subscription.setUser(user);
//                     subscription.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                     subscription.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                 }
//                 subscription = subscriptionRepository.save(subscription);
//                 subscriptionsFound++;

//                 for (Transaction tx : merchantTransactions) {
//                     SubscriptionTransactionId id = new SubscriptionTransactionId(subscription.getId(), tx.getId());
//                     if (!subscriptionTransactionRepository.existsById(id)) {
//                         SubscriptionTransaction link = new SubscriptionTransaction();
//                         link.setId(id);
//                         subscriptionTransactionRepository.save(link);
//                     }
//                 }
//             }

//             history.setSubscriptionsFound(subscriptionsFound);
//             history.setStatus("completed");
//             history.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
//             scanHistoryRepository.save(history);
//         } catch (Exception ex) {
//             log.error("runScan failed for user {}", userId, ex);
//             history.setStatus("failed");
//             history.setErrorMessage(ex.getMessage());
//             history.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
//             scanHistoryRepository.save(history);
//         }
//     }
// }

package com.shadowspend.service;

import com.shadowspend.model.Email;
import com.shadowspend.model.ScanHistory;
import com.shadowspend.model.Subscription;
import com.shadowspend.model.SubscriptionTransaction;
import com.shadowspend.model.SubscriptionTransactionId;
import com.shadowspend.model.Transaction;
import com.shadowspend.model.User;
import com.shadowspend.repository.EmailRepository;
import com.shadowspend.repository.ScanHistoryRepository;
import com.shadowspend.repository.SubscriptionRepository;
import com.shadowspend.repository.SubscriptionTransactionRepository;
import com.shadowspend.repository.TransactionRepository;
import com.shadowspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanOrchestrator {

    private final ScanHistoryRepository scanHistoryRepository;
    private final UserRepository userRepository;
    private final GmailService gmailService;
    private final EmailRepository emailRepository;
    private final EmailParserService emailParserService;
    private final TransactionRepository transactionRepository;
    private final DetectionService detectionService;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionTransactionRepository subscriptionTransactionRepository;

    @Async
    public void runScan(UUID userId, UUID scanId) {
        log.info("runScan called for user {} and scan {}", userId, scanId);
        ScanHistory history = scanHistoryRepository.findById(scanId).orElse(null);
        if (history == null) {
            return;
        }
        try {
            if (scanHistoryRepository.countByUserIdAndStatus(userId, "running") > 1) {
                history.setStatus("skipped");
                history.setErrorMessage("Another scan is already running");
                history.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
                scanHistoryRepository.save(history);
                return;
            }
            history.setStatus("running");
            scanHistoryRepository.save(history);
            User user = userRepository.findById(userId).orElseThrow();
            user = gmailService.refreshTokenIfNeeded(user);
            int fetched = gmailService.fetchEmails(user);
            history.setEmailsFetched(fetched);
            List<Email> unprocessed = emailRepository.findByUserIdAndProcessedFalse(userId);
            int parsedCount = 0;
            for (Email email : unprocessed) {
                try {
                    if (Boolean.TRUE.equals(email.getProcessed())) {
                        continue;
                    }
                    Transaction parsed = emailParserService.parseEmail(email);
                    if (parsed != null && !transactionRepository.existsByEmailId(email.getId())) {
                        transactionRepository.save(parsed);
                        parsedCount++;
                    }
                    email.setProcessed(Boolean.TRUE);
                    emailRepository.save(email);
                } catch (Exception ex) {
                    log.error("Error while parsing email {}", email.getId(), ex);
                    email.setProcessingError(ex.getMessage());
                    email.setProcessed(Boolean.TRUE);
                    emailRepository.save(email);
                }
            }
            history.setEmailsParsed(parsedCount);

            List<Transaction> transactions = transactionRepository.findByUserId(userId);
            Map<String, List<Transaction>> grouped = detectionService.groupTransactionsByMerchant(transactions);
            int subscriptionsFound = 0;
            for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {
                String merchant = entry.getKey();
                List<Transaction> merchantTransactions = entry.getValue();
                List<Email> emails = new ArrayList<>();
                for (Transaction tx : merchantTransactions) {
                    if (tx.getEmail() != null) {
                        emails.add(tx.getEmail());
                    }
                }
                Subscription detected = detectionService.buildSubscription(merchant, merchantTransactions, emails);

                // ✅ Skip financial and bill categories
                if ("financial".equals(detected.getCategory()) ||
                    "bill".equals(detected.getCategory())) {
                    log.info("Skipping financial/bill category for merchant: {}", merchant);
                    continue;
                }

                // ✅ Skip unknown low confidence subscriptions
                if ("UNKNOWN".equals(detected.getClassification()) &&
                    detected.getConfidenceScore().compareTo(new BigDecimal("0.4")) < 0) {
                    log.info("Skipping low confidence UNKNOWN subscription: {}", merchant);
                    continue;
                }

                Subscription subscription = subscriptionRepository.findByUserIdAndNormalizedServiceName(userId, merchant).orElse(null);
                if (subscription != null) {
                    subscription.setAmount(detected.getAmount());
                    subscription.setOccurrenceCount(detected.getOccurrenceCount());
                    subscription.setNextBillingDate(detected.getNextBillingDate());
                    subscription.setConfidenceScore(detected.getConfidenceScore());
                    subscription.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                } else {
                    subscription = detected;
                    subscription.setUser(user);
                    subscription.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    subscription.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                }
                subscription = subscriptionRepository.save(subscription);
                subscriptionsFound++;

                for (Transaction tx : merchantTransactions) {
                    SubscriptionTransactionId id = new SubscriptionTransactionId(subscription.getId(), tx.getId());
                    if (!subscriptionTransactionRepository.existsById(id)) {
                        SubscriptionTransaction link = new SubscriptionTransaction();
                        link.setId(id);
                        subscriptionTransactionRepository.save(link);
                    }
                }
            }

            history.setSubscriptionsFound(subscriptionsFound);
            history.setStatus("completed");
            history.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
            scanHistoryRepository.save(history);
        } catch (Exception ex) {
            log.error("runScan failed for user {}", userId, ex);
            history.setStatus("failed");
            history.setErrorMessage(ex.getMessage());
            history.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
            scanHistoryRepository.save(history);
        }
    }
}