package com.shadowspend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ScanStatusDTO {
    private UUID scanId;
    private String status;
    private Integer emailsFetched;
    private Integer emailsParsed;
    private Integer subscriptionsFound;
    private String errorMessage;
}
