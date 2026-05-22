package com.shadowspend.service;

import com.shadowspend.model.Email;
import com.shadowspend.model.Subscription;
import com.shadowspend.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionService {

    public Map<String, List<Transaction>> groupTransactionsByMerchant(List<Transaction> transactions) {
        log.info("groupTransactionsByMerchant called");
        return transactions.stream().collect(Collectors.groupingBy(Transaction::getNormalizedMerchantName));
    }

    public String detectBillingCycle(List<Transaction> transactions) {
        log.info("detectBillingCycle called");
        if (transactions == null || transactions.size() <= 1) {
            return "unknown";
        }
        List<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort(Comparator.comparing(Transaction::getTransactionDate));
        long totalGaps = 0;
        int gapCount = 0;
        for (int i = 1; i < sorted.size(); i++) {
            long days = ChronoUnit.DAYS.between(sorted.get(i - 1).getTransactionDate().toLocalDate(), sorted.get(i).getTransactionDate().toLocalDate());
            totalGaps += days;
            gapCount++;
        }
        if (gapCount == 0) {
            return "unknown";
        }
        double avg = (double) totalGaps / gapCount;
        if (avg >= 15 && avg <= 45) {
            return "monthly";
        }
        if (avg >= 300 && avg <= 400) {
            return "yearly";
        }
        return "irregular";
    }

    public String classify(List<Transaction> transactions, String cycle) {
        log.info("classify called");
        int count = transactions == null ? 0 : transactions.size();
        if (count >= 3 && ("monthly".equals(cycle) || "yearly".equals(cycle))) {
            return "CONFIRMED";
        }
        if (count == 2 && !"unknown".equals(cycle)) {
            return "LIKELY";
        }
        if (count == 1 && transactions.stream().anyMatch(t -> Boolean.TRUE.equals(t.getIsRecurringSignal()))) {
            return "POTENTIAL";
        }
        return "UNKNOWN";
    }

    public BigDecimal calculateConfidence(List<Transaction> transactions, String cycle, String classification) {
        log.info("calculateConfidence called");
        int count = transactions == null ? 0 : transactions.size();
        double score = Math.min(count * 0.15, 0.6);
        if ("monthly".equals(cycle) || "yearly".equals(cycle)) {
            score += 0.2;
        }
        if (transactions.stream().anyMatch(t -> Boolean.TRUE.equals(t.getIsRecurringSignal()))) {
            score += 0.15;
        }
        if (amountsWithin10Percent(transactions)) {
            score += 0.05;
        }
        return BigDecimal.valueOf(Math.min(1.0, score));
    }

    public LocalDate predictNextBillingDate(List<Transaction> transactions, String cycle) {
        log.info("predictNextBillingDate called");
        LocalDate lastDate = transactions.stream().map(t -> t.getTransactionDate().toLocalDate()).max(LocalDate::compareTo).orElse(LocalDate.now());
        if ("monthly".equals(cycle)) {
            return lastDate.plusDays(30);
        }
        if ("yearly".equals(cycle)) {
            return lastDate.plusDays(365);
        }
        return lastDate.plusDays(30);
    }

    public String inferAutopay(List<Email> emails) {
        log.info("inferAutopay called");
        try {
            boolean found = emails.stream().anyMatch(email -> {
                try {
                    String body = email.getRawBody() == null ? "" : email.getRawBody().toLowerCase();
                    return body.contains("auto-debit") || body.contains("autopay") || body.contains("auto-renewed");
                } catch (Exception e) {
                    return false;
                }
            });
            return found ? "likely" : "unknown";
        } catch (Exception e) {
            log.warn("inferAutopay failed, defaulting to unknown", e);
            return "unknown";
        }
    }

    public Subscription buildSubscription(String merchant, List<Transaction> transactions, List<Email> emails) {
        log.info("buildSubscription called for merchant {}", merchant);
        String cycle = detectBillingCycle(transactions);
        String classification = classify(transactions, cycle);
        BigDecimal confidence = calculateConfidence(transactions, cycle, classification);
        Transaction latest = transactions.stream().max(Comparator.comparing(Transaction::getTransactionDate)).orElse(null);
        Subscription subscription = new Subscription();
        subscription.setMerchantName(latest != null ? latest.getRawMerchantName() : merchant);
        subscription.setNormalizedServiceName(merchant);
        subscription.setBillingCycle(cycle);
        subscription.setClassification(classification);
        subscription.setConfidenceScore(confidence);
        subscription.setAmount(latest == null ? BigDecimal.ZERO : latest.getAmount());
        subscription.setCurrency(latest == null ? "INR" : latest.getCurrency());
        subscription.setLastBilledDate(latest == null ? null : latest.getTransactionDate().toLocalDate());
        subscription.setNextBillingDate(predictNextBillingDate(transactions, cycle));
        subscription.setOccurrenceCount(transactions.size());
        subscription.setAutopayStatus(inferAutopay(emails));
        subscription.setIsActive(Boolean.TRUE);
        subscription.setUserVerified(Boolean.FALSE);
        subscription.setCategory(inferCategory(merchant));
        return subscription;
    }

    private boolean amountsWithin10Percent(List<Transaction> transactions) {
        if (transactions == null || transactions.size() < 2) {
            return false;
        }
        BigDecimal min = transactions.stream().map(Transaction::getAmount).min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        BigDecimal max = transactions.stream().map(Transaction::getAmount).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        if (min.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal ratio = max.subtract(min).divide(min, 4, RoundingMode.HALF_UP);
        return ratio.compareTo(BigDecimal.valueOf(0.10)) <= 0;
    }

    private String inferCategory(String merchant) {
        String text = merchant == null ? "" : merchant.toLowerCase();
        if (text.contains("bank") || text.contains("pay") || text.contains("wallet")) {
            return "Financial";
        }
        if (text.contains("electric") || text.contains("gas") || text.contains("water") || text.contains("bill")) {
            return "Bill";
        }
        if (text.isBlank()) {
            return "Unknown";
        }
        return "Subscription";
    }
}