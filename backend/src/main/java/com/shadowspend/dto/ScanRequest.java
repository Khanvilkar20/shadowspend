package com.shadowspend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ScanRequest {
    @NotNull
    private UUID userId;
}
