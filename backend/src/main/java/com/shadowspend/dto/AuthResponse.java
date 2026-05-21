package com.shadowspend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private UUID userId;
    private String email;
    private String name;
    private String pictureUrl;
}
