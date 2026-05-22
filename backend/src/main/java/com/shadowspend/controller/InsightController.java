// package com.shadowspend.controller;

// import com.shadowspend.dto.InsightDTO;
// import com.shadowspend.dto.SubscriptionDTO;
// import com.shadowspend.model.Subscription;
// import com.shadowspend.repository.SubscriptionRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Map;
// import java.util.UUID;
// import java.util.stream.Collectors;

// @RestController
// @RequestMapping("/api/insights")
// @RequiredArgsConstructor
// public class InsightController {

//     private final SubscriptionRepository subscriptionRepository;

//     @GetMapping("/{userId}")
//     public InsightDTO insights(@PathVariable UUID userId) {
//         List<Subscription> subscriptions = subscriptionRepository.findByUserIdAndIsActiveTrue(userId);
//         BigDecimal totalMonthlySpend = subscriptions.stream()
//                 .filter(s -> "monthly".equalsIgnoreCase(s.getBillingCycle()))
//                 .filter(s -> "CONFIRMED".equalsIgnoreCase(s.getClassification()) || "LIKELY".equalsIgnoreCase(s.getClassification()))
//                 .map(Subscription::getAmount)
//                 .filter(a -> a != null)
//                 .reduce(BigDecimal.ZERO, BigDecimal::add);
//         LocalDate upcomingDate = LocalDate.now().plusDays(7);
//         List<Subscription> upcoming = subscriptions.stream()
//                 .filter(s -> s.getNextBillingDate() != null && !s.getNextBillingDate().isAfter(upcomingDate))
//                 .toList();
//         BigDecimal upcomingAmount = upcoming.stream()
//                 .map(Subscription::getAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add);
//         List<SubscriptionDTO> topServices = subscriptions.stream()
//                 .sorted(Comparator.comparing(Subscription::getAmount, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
//                 .limit(5)
//                 .map(s -> SubscriptionDTO.builder()
//                         .id(s.getId()).merchantName(s.getMerchantName()).normalizedServiceName(s.getNormalizedServiceName())
//                         .amount(s.getAmount()).currency(s.getCurrency()).category(s.getCategory())
//                         .billingCycle(s.getBillingCycle()).classification(s.getClassification())
//                         .build())
//                 .toList();
//         Map<String, Long> categoryBreakdown = subscriptions.stream()
//                 .collect(Collectors.groupingBy(s -> s.getCategory() == null ? "unknown" : s.getCategory(), Collectors.counting()));
//         return InsightDTO.builder()
//                 .totalMonthlySpend(totalMonthlySpend)
//                 .upcomingCount(upcoming.size())
//                 .upcomingAmount(upcomingAmount)
//                 .topServices(topServices)
//                 .categoryBreakdown(categoryBreakdown)
//                 .build();
//     }
// }


package com.shadowspend.controller;

import com.shadowspend.dto.InsightDTO;
import com.shadowspend.dto.SubscriptionDTO;
import com.shadowspend.model.Subscription;
import com.shadowspend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final SubscriptionRepository subscriptionRepository;

    @GetMapping("/{userId}")
    public InsightDTO insights(@PathVariable UUID userId) {

        List<Subscription> subscriptions =
                subscriptionRepository.findByUserIdAndIsActiveTrue(userId);

        // ✅ TOTAL MONTHLY SPEND (normalized)
        BigDecimal totalMonthlySpend = subscriptions.stream()
                .filter(s -> "CONFIRMED".equalsIgnoreCase(s.getClassification())
                        || "LIKELY".equalsIgnoreCase(s.getClassification()))
                .map(this::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ✅ UPCOMING (next 7 days)
        LocalDate upcomingDate = LocalDate.now().plusDays(7);

        List<Subscription> upcoming = subscriptions.stream()
                .filter(s -> s.getNextBillingDate() != null &&
                        !s.getNextBillingDate().isAfter(upcomingDate))
                .toList();

        BigDecimal upcomingAmount = upcoming.stream()
                .map(this::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ✅ TOP SERVICES (normalized)
        List<SubscriptionDTO> topServices = subscriptions.stream()
                .sorted(Comparator.comparing(this::getMonthlyAmount).reversed())
                .limit(5)
                .map(s -> SubscriptionDTO.builder()
                        .id(s.getId())
                        .merchantName(s.getMerchantName())
                        .normalizedServiceName(s.getNormalizedServiceName())
                        .amount(getMonthlyAmount(s)) // ✅ FIXED
                        .currency(s.getCurrency())
                        .category(s.getCategory())
                        .billingCycle(s.getBillingCycle())
                        .classification(s.getClassification())
                        .build())
                .toList();

        // ✅ CATEGORY BREAKDOWN (SUM, NOT COUNT)
        Map<String, BigDecimal> categoryBreakdown = subscriptions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCategory() == null ? "Unknown" : s.getCategory(),
                        Collectors.mapping(
                                this::getMonthlyAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        return InsightDTO.builder()
                .totalMonthlySpend(totalMonthlySpend)
                .upcomingCount(upcoming.size())
                .upcomingAmount(upcomingAmount)
                .topServices(topServices)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }

    // ✅ HELPER: normalize yearly → monthly
    private BigDecimal getMonthlyAmount(Subscription s) {
        if (s.getAmount() == null) return BigDecimal.ZERO;

        if ("yearly".equalsIgnoreCase(s.getBillingCycle())) {
            return s.getAmount()
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        }

        return s.getAmount();
    }
}