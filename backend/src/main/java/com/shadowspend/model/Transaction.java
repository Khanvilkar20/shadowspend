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
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_id")
    private Email email;
    @Column(name = "raw_merchant_name")
    private String rawMerchantName;
    @Column(name = "normalized_merchant_name")
    private String normalizedMerchantName;
    private BigDecimal amount;
    private String currency;
    @Column(name = "transaction_date")
    private OffsetDateTime transactionDate;
    @Column(name = "parsing_confidence")
    private BigDecimal parsingConfidence;
    @Column(name = "parse_method")
    private String parseMethod;
    @Column(name = "is_recurring_signal")
    private Boolean isRecurringSignal;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
