package com.shadowspend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class SubscriptionTransactionId implements Serializable {
    @Column(name = "subscription_id")
    private UUID subscriptionId;
    @Column(name = "transaction_id")
    private UUID transactionId;
}
