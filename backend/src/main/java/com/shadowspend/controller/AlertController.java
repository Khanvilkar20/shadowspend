package com.shadowspend.controller;

import com.shadowspend.dto.AlertDTO;
import com.shadowspend.model.Alert;
import com.shadowspend.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping("/{userId}")
    public List<AlertDTO> list(@PathVariable UUID userId) {
        return alertRepository.findByUserIdAndSentFalseAndDismissedFalse(userId).stream().map(a -> AlertDTO.builder()
                .id(a.getId())
                .subscriptionId(a.getSubscription() == null ? null : a.getSubscription().getId())
                .alertType(a.getAlertType())
                .message(a.getMessage())
                .scheduledAt(a.getScheduledAt())
                .sent(a.getSent())
                .dismissed(a.getDismissed())
                .build()).toList();
    }

    @PatchMapping("/{id}/dismiss")
    public void dismiss(@PathVariable UUID id) {
        Alert alert = alertRepository.findById(id).orElseThrow();
        alert.setDismissed(Boolean.TRUE);
        alertRepository.save(alert);
    }
}
