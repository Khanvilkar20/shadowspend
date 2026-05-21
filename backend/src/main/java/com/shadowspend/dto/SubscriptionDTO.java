package com.shadowspend.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class SubscriptionDTO {
    private UUID id;
    private String merchantName;
    private String normalizedServiceName;
    private String category;
    private BigDecimal amount;
    private String currency;
    private String billingCycle;
    private String classification;
    private BigDecimal confidenceScore;
    private String autopayStatus;
    private LocalDate nextBillingDate;
    private Integer occurrenceCount;
    private Boolean isActive;
    private Boolean userVerified;
}
