package com.shadowspend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    private String email;
    private String name;
    @Column(name = "picture_url")
    private String pictureUrl;
    @Column(name = "google_access_token")
    private String googleAccessToken;
    @Column(name = "google_refresh_token")
    private String googleRefreshToken;
    @Column(name = "token_expiry")
    private OffsetDateTime tokenExpiry;
    @Column(name = "is_demo")
    private Boolean isDemo;
    @Column(name = "last_scan_at")
    private OffsetDateTime lastScanAt;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
