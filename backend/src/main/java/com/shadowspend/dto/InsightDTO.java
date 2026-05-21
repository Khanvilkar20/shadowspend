package com.shadowspend.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class InsightDTO {
    private BigDecimal totalMonthlySpend;
    private Integer upcomingCount;
    private BigDecimal upcomingAmount;
    private List<SubscriptionDTO> topServices;
    // private Map<String, Long> categoryBreakdown;
    Map<String, BigDecimal> categoryBreakdown;
}
