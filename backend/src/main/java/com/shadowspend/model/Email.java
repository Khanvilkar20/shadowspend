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

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "emails")
public class Email {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "gmail_id")
    private String gmailId;
    private String subject;
    private String sender;
    @Column(name = "sender_domain")
    private String senderDomain;
    @Column(name = "raw_body")
    private String rawBody;
    private String snippet;
    @Column(name = "received_at")
    private OffsetDateTime receivedAt;
    private Boolean processed;
    @Column(name = "processing_error")
    private String processingError;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
