package com.shadowspend.service;

import com.shadowspend.model.Alert;
import com.shadowspend.model.Subscription;
import com.shadowspend.repository.AlertRepository;
import com.shadowspend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final SubscriptionRepository subscriptionRepository;
    private final AlertRepository alertRepository;
    private final JavaMailSender mailSender;

    @Scheduled(cron = "0 0 9 * * ?")
    public void scheduledAlerts() {
        log.info("scheduledAlerts called");
        LocalDate cutoff = LocalDate.now().plusDays(3);
        List<Subscription> due = subscriptionRepository.findByNextBillingDateBeforeAndIsActiveTrue(cutoff);
        OffsetDateTime todayStart = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);
        for (Subscription subscription : due) {
            try {
                if (alertRepository.existsBySubscriptionIdAndScheduledAtAfter(subscription.getId(), todayStart)) {
                    continue;
                }
                Alert alert = new Alert();
                alert.setUser(subscription.getUser());
                alert.setSubscription(subscription);
                alert.setAlertType("renewal");
                alert.setMessage("Upcoming renewal for " + subscription.getMerchantName());
                alert.setScheduledAt(OffsetDateTime.now(ZoneOffset.UTC));
                alert.setSent(Boolean.FALSE);
                alert.setDismissed(Boolean.FALSE);
                alert.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                alert = alertRepository.save(alert);
                try {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(subscription.getUser().getEmail());
                    message.setSubject("ShadowSpend renewal alert");
                    message.setText(alert.getMessage());
                    mailSender.send(message);
                    alert.setSent(Boolean.TRUE);
                    alert.setSentAt(OffsetDateTime.now(ZoneOffset.UTC));
                    alertRepository.save(alert);
                } catch (Exception mailEx) {
                    log.error("Alert email sending failed for subscription {}", subscription.getId(), mailEx);
                }
            } catch (Exception ex) {
                log.error("Failed scheduled alert for subscription {}", subscription.getId(), ex);
            }
        }
    }
}
