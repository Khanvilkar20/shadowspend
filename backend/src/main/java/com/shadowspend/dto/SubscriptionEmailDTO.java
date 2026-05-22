package com.shadowspend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class SubscriptionEmailDTO {
    private UUID id;
    private String subject;
    private String sender;
    private String snippet;
    private String rawBody;
    private OffsetDateTime receivedAt;
    private double amount;
}
