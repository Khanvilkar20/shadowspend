// package com.shadowspend.service;

// import com.shadowspend.model.Email;
// import com.shadowspend.model.Transaction;
// import com.shadowspend.repository.EmailRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.OffsetDateTime;
// import java.time.ZoneOffset;
// import java.time.format.DateTimeFormatter;
// import java.time.format.DateTimeParseException;
// import java.util.List;
// import java.util.Locale;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class EmailParserService {

//     private final EmailRepository emailRepository;
//     private final NormalizationService normalizationService;

//     public Transaction parseEmail(Email email) {
//         log.info("parseEmail called for email {}", email.getId());
//         try {
//             String body = email.getRawBody() == null ? "" : email.getRawBody();
//             String merchant = extractMerchant(body, email.getSender(), email.getSenderDomain());
//             BigDecimal amount = extractAmount(body);
//             if (amount == null) {
//                 return null;
//             }
//             OffsetDateTime txDate = extractDate(body, email.getReceivedAt());
//             boolean recurringSignal = detectRecurringSignal(body);
//             double confidence = calculateParsingConfidence(merchant != null, amount != null, txDate != null, recurringSignal);

//             Transaction transaction = new Transaction();
//             transaction.setUser(email.getUser());
//             transaction.setEmail(email);
//             transaction.setRawMerchantName(merchant);
//             transaction.setNormalizedMerchantName(normalizationService.normalizeMerchant(merchant));
//             transaction.setAmount(amount);
//             transaction.setCurrency(extractCurrency(body));
//             transaction.setTransactionDate(txDate == null ? email.getReceivedAt() : txDate);
//             transaction.setParsingConfidence(BigDecimal.valueOf(confidence));
//             transaction.setParseMethod("regex");
//             transaction.setIsRecurringSignal(recurringSignal);
//             transaction.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
//             return transaction;
//         } catch (Exception ex) {
//             log.error("parseEmail failed for email {}", email.getId(), ex);
//             email.setProcessingError(ex.getMessage());
//             email.setProcessed(Boolean.TRUE);
//             emailRepository.save(email);
//             return null;
//         }
//     }

//     private String extractMerchant(String body, String sender, String senderDomain) {
//         if (sender != null && !sender.isBlank()) {
//             return sender;
        
//         }
//         if (senderDomain != null && !senderDomain.isBlank()) {
//             return senderDomain;
//         }
//         if (body == null) {
//             return "unknown";
//         }
//         Matcher matcher = Pattern.compile("(?i)(netflix|spotify|amazon|google|jio|hotstar|youtube|microsoft|adobe|swiggy|zomato|phonepe|paytm)").matcher(body);
//         return matcher.find() ? matcher.group(1) : "unknown";
//     }

//     private BigDecimal extractAmount(String body) {
//         List<Pattern> patterns = List.of(
//                 Pattern.compile("(?i)₹\\s*([0-9,]+(?:\\.[0-9]{1,2})?)"),
//                 Pattern.compile("(?i)rs\\.?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)"),
//                 Pattern.compile("(?i)\\$\\s*([0-9,]+(?:\\.[0-9]{1,2})?)"),
//                 Pattern.compile("(?i)inr\\s*([0-9,]+(?:\\.[0-9]{1,2})?)"),
//                 Pattern.compile("(?i)\\b([0-9]{2,}(?:\\.[0-9]{1,2})?)\\b")
//         );
//         for (Pattern pattern : patterns) {
//             Matcher matcher = pattern.matcher(body == null ? "" : body);
//             if (matcher.find()) {
//                 String cleaned = matcher.group(1).replace(",", "");
//                 try {
//                     return new BigDecimal(cleaned);
//                 } catch (Exception ex) {
//                     log.error("Failed to parse amount {}", cleaned, ex);
//                 }
//             }
//         }
//         return null;
//     }

//     private OffsetDateTime extractDate(String body, OffsetDateTime fallback) {
//         List<DateTimeFormatter> formatters = List.of(
//                 DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
//                 DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
//                 DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
//                 DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
//         );
//         Matcher matcher = Pattern.compile("(\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}|\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4})").matcher(body == null ? "" : body);
//         if (matcher.find()) {
//             String found = matcher.group(1);
//             for (DateTimeFormatter formatter : formatters) {
//                 try {
//                     LocalDate date = LocalDate.parse(found, formatter);
//                     return date.atStartOfDay().atOffset(ZoneOffset.UTC);
//                 } catch (DateTimeParseException ignored) {
//                 }
//             }
//         }
//         return fallback;
//     }

//     private boolean detectRecurringSignal(String body) {
//         String text = body == null ? "" : body.toLowerCase();
//         return text.contains("renewal") || text.contains("monthly") || text.contains("auto-renewed")
//                 || text.contains("subscription") || text.contains("recurring")
//                 || text.contains("next billing") || text.contains("auto-debit") || text.contains("autopay");
//     }

//     private double calculateParsingConfidence(boolean merchant, boolean amount, boolean date, boolean recurring) {
//         double score = 0.0;
//         if (merchant) {
//             score += 0.3;
//         }
//         if (amount) {
//             score += 0.3;
//         }
//         if (date) {
//             score += 0.2;
//         }
//         if (recurring) {
//             score += 0.2;
//         }
//         return Math.min(1.0, score);
//     }

//     private String extractCurrency(String body) {
//         String text = body == null ? "" : body.toLowerCase();
//         if (text.contains("₹") || text.contains("rs") || text.contains("inr")) {
//             return "INR";
//         }
//         if (text.contains("$")) {
//             return "USD";
//         }
//         return "INR";
//     }
// }

package com.shadowspend.service;

import com.shadowspend.model.Email;
import com.shadowspend.model.Transaction;
import com.shadowspend.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailParserService {

    private final EmailRepository emailRepository;
    private final NormalizationService normalizationService;

    // Known subscription services only
    private static final Pattern KNOWN_SERVICES = Pattern.compile(
        "(?i)\\b(netflix|spotify|amazon prime|amazon|youtube premium|youtube|" +
        "hotstar|disney|microsoft|microsoft 365|office 365|xbox|pc game pass|game pass|" +
        "adobe|canva|figma|notion|slack|zoom|github|dropbox|" +
        "icloud|apple one|apple tv|apple music|google one|google|" +
        "chatgpt|openai|midjourney|grammarly|linkedin|coursera|udemy|" +
        "jio|airtel|phonepe|paytm|swiggy one|zomato pro|" +
        "claude|anthropic|gemini|copilot|perplexity)\\b"
    );

    // Payment confirmation keywords
    private static final Pattern PAYMENT_KEYWORDS = Pattern.compile(
        "(?i)(charged|payment successful|payment received|receipt|invoice|" +
        "renewed|subscription renewed|debited|thank you for your payment|" +
        "your payment|amount paid|successfully processed|order confirmed|" +
        "billing|your subscription|membership renewed)"
    );

    // Bank/spam patterns to skip
    private static final Pattern SKIP_PATTERNS = Pattern.compile(
        "(?i)(otp|one.time.password|verification code|bank alert|" +
        "transaction alert|debit alert|credit alert|account alert|" +
        "kotak|hdfc|sbi|icici|axis bank|yes bank|idbi|" +
        "electricity|water bill|gas bill|insurance premium|" +
        "salary|payslip|naukri|job alert|matrimon)"
    );

    public Transaction parseEmail(Email email) {
        log.info("parseEmail called for email {}", email.getId());
        try {
            String body = email.getRawBody() == null ? "" : email.getRawBody();
            String subject = email.getSubject() == null ? "" : email.getSubject();
            String sender = email.getSender() == null ? "" : email.getSender();
            String combined = subject + " " + body + " " + sender;

            // Step 1: Skip bank/spam/irrelevant emails
            if (SKIP_PATTERNS.matcher(combined).find()) {
                log.info("Skipping bank/spam email: {}", subject);
                return null;
            }

            // Step 2: Must contain a known subscription service
            Matcher serviceMatcher = KNOWN_SERVICES.matcher(combined);
            if (!serviceMatcher.find()) {
                log.info("Skipping non-subscription email: {}", subject);
                return null;
            }
            String detectedService = serviceMatcher.group(1);

            // Step 3: Must be a payment email (not just marketing)
            if (!PAYMENT_KEYWORDS.matcher(combined).find()) {
                log.info("Skipping marketing email (no payment signal): {}", subject);
                return null;
            }

            // Step 4: Extract merchant - use detected service name
            String merchant = extractMerchant(detectedService, subject, sender, email.getSenderDomain());

            // Step 5: Extract amount
            BigDecimal amount = extractAmount(combined);
            if (amount == null) {
                // Still create transaction with 0 amount if it's a confirmed subscription
                amount = BigDecimal.ZERO;
            }

            OffsetDateTime txDate = extractDate(body, email.getReceivedAt());
            boolean recurringSignal = detectRecurringSignal(combined);
            double confidence = calculateParsingConfidence(
                merchant != null, true, txDate != null, recurringSignal
            );

            Transaction transaction = new Transaction();
            transaction.setUser(email.getUser());
            transaction.setEmail(email);
            transaction.setRawMerchantName(merchant);
            transaction.setNormalizedMerchantName(normalizationService.normalizeMerchant(merchant));
            transaction.setAmount(amount);
            transaction.setCurrency(extractCurrency(combined));
            transaction.setTransactionDate(txDate == null ? email.getReceivedAt() : txDate);
            transaction.setParsingConfidence(BigDecimal.valueOf(confidence));
            transaction.setParseMethod("regex");
            transaction.setIsRecurringSignal(recurringSignal);
            transaction.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            return transaction;

        } catch (Exception ex) {
            log.error("parseEmail failed for email {}", email.getId(), ex);
            email.setProcessingError(ex.getMessage());
            email.setProcessed(Boolean.TRUE);
            emailRepository.save(email);
            return null;
        }
    }

    private String extractMerchant(String detectedService, String subject, String sender, String senderDomain) {
        // Use the detected known service name as primary merchant
        if (detectedService != null && !detectedService.isBlank()) {
            return detectedService;
        }

        // Clean sender name - remove email address part
        if (sender != null && !sender.isBlank()) {
            String cleanSender = sender.replaceAll("<[^>]+>", "").trim();
            cleanSender = cleanSender.replaceAll("[\"']", "").trim();
            if (!cleanSender.isBlank() && cleanSender.length() < 50) {
                return cleanSender;
            }
        }

        // Extract from domain
        if (senderDomain != null && !senderDomain.isBlank()) {
            String[] parts = senderDomain.split("\\.");
            if (parts.length >= 2) {
                return parts[parts.length - 2];
            }
            return senderDomain;
        }

        return "unknown";
    }

    private BigDecimal extractAmount(String body) {
        List<Pattern> patterns = List.of(
                Pattern.compile("(?i)₹\\s*([0-9,]+(?:\\.[0-9]{1,2})?)"),
                Pattern.compile("(?i)rs\\.?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)"),
                Pattern.compile("(?i)\\$\\s*([0-9,]+(?:\\.[0-9]{1,2})?)"),
                Pattern.compile("(?i)inr\\s*([0-9,]+(?:\\.[0-9]{1,2})?)"),
                Pattern.compile("(?i)amount[:\\s]+([0-9,]+(?:\\.[0-9]{1,2})?)")
        );
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(body == null ? "" : body);
            if (matcher.find()) {
                String cleaned = matcher.group(1).replace(",", "");
                try {
                    BigDecimal amount = new BigDecimal(cleaned);
                    if (amount.compareTo(BigDecimal.TEN) >= 0 &&
                        amount.compareTo(new BigDecimal("50000")) <= 0) {
                        return amount;
                    }
                } catch (Exception ex) {
                    log.error("Failed to parse amount {}", cleaned, ex);
                }
            }
        }
        return null;
    }

    private OffsetDateTime extractDate(String body, OffsetDateTime fallback) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
        );
        Matcher matcher = Pattern.compile(
            "(\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}|\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4})"
        ).matcher(body == null ? "" : body);
        if (matcher.find()) {
            String found = matcher.group(1);
            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDate date = LocalDate.parse(found, formatter);
                    return date.atStartOfDay().atOffset(ZoneOffset.UTC);
                } catch (DateTimeParseException ignored) {}
            }
        }
        return fallback;
    }

    private boolean detectRecurringSignal(String body) {
        String text = body == null ? "" : body.toLowerCase();
        return text.contains("renewal") || text.contains("monthly") ||
               text.contains("auto-renewed") || text.contains("subscription") ||
               text.contains("recurring") || text.contains("next billing") ||
               text.contains("auto-debit") || text.contains("autopay") ||
               text.contains("annually") || text.contains("yearly");
    }

    private double calculateParsingConfidence(boolean merchant, boolean amount, boolean date, boolean recurring) {
        double score = 0.0;
        if (merchant) score += 0.3;
        if (amount) score += 0.3;
        if (date) score += 0.2;
        if (recurring) score += 0.2;
        return Math.min(1.0, score);
    }

    private String extractCurrency(String body) {
        String text = body == null ? "" : body.toLowerCase();
        if (text.contains("₹") || text.contains("rs") || text.contains("inr")) return "INR";
        if (text.contains("$") || text.contains("usd")) return "USD";
        return "INR";
    }
}