package com.shadowspend.controller;

import com.shadowspend.dto.SubscriptionDTO;
import com.shadowspend.dto.SubscriptionEmailDTO;
import com.shadowspend.model.Subscription;
import com.shadowspend.repository.SubscriptionRepository;
import com.shadowspend.service.SubscriptionEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionEmailService subscriptionEmailService;

    @GetMapping("/{userId}")
    public List<SubscriptionDTO> list(@PathVariable UUID userId, @RequestParam(required = false) String category) {
        List<Subscription> subscriptions = category == null || category.isBlank()
                ? subscriptionRepository.findByUserIdAndIsActiveTrue(userId)
                : subscriptionRepository.findByUserIdAndCategory(userId, category).stream().filter(s -> Boolean.TRUE.equals(s.getIsActive())).toList();
        return subscriptions.stream().map(this::toDTO).toList();
    }

    @GetMapping("/{id}/emails")
    public List<SubscriptionEmailDTO> emails(@PathVariable UUID id) {
        return subscriptionEmailService.getEmailsForSubscription(id);
    }

    @PatchMapping("/{id}/verify")
    public SubscriptionDTO verify(@PathVariable UUID id) {
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow();
        subscription.setUserVerified(Boolean.TRUE);
        subscription = subscriptionRepository.save(subscription);
        return toDTO(subscription);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivate(@PathVariable UUID id) {
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow();
        subscription.setIsActive(Boolean.FALSE);
        subscriptionRepository.save(subscription);
    }

    private SubscriptionDTO toDTO(Subscription s) {
        return SubscriptionDTO.builder()
                .id(s.getId())
                .merchantName(s.getMerchantName())
                .normalizedServiceName(s.getNormalizedServiceName())
                .category(s.getCategory())
                .amount(s.getAmount())
                .currency(s.getCurrency())
                .billingCycle(s.getBillingCycle())
                .classification(s.getClassification())
                .confidenceScore(s.getConfidenceScore())
                .autopayStatus(s.getAutopayStatus())
                .nextBillingDate(s.getNextBillingDate())
                .occurrenceCount(s.getOccurrenceCount())
                .isActive(s.getIsActive())
                .userVerified(s.getUserVerified())
                .build();
    }
}
