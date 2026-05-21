package com.shadowspend.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subscription_transactions")
public class SubscriptionTransaction {
    @EmbeddedId
    private SubscriptionTransactionId id;
}
