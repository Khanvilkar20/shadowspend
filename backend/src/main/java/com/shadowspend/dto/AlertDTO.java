package com.shadowspend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class AlertDTO {
    private UUID id;
    private UUID subscriptionId;
    private String alertType;
    private String message;
    private OffsetDateTime scheduledAt;
    private Boolean sent;
    private Boolean dismissed;
}
