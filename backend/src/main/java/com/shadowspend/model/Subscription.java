package com.shadowspend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "merchant_name")
    private String merchantName;
    @Column(name = "normalized_service_name")
    private String normalizedServiceName;
    private String category;
    private BigDecimal amount;
    private String currency;
    @Column(name = "billing_cycle")
    private String billingCycle;
    private String classification;
    @Column(name = "confidence_score")
    private BigDecimal confidenceScore;
    @Column(name = "autopay_status")
    private String autopayStatus;
    @Column(name = "last_billed_date")
    private LocalDate lastBilledDate;
    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;
    @Column(name = "occurrence_count")
    private Integer occurrenceCount;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "user_verified")
    private Boolean userVerified;
    private String notes;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
