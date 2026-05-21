package com.shadowspend.controller;

import com.shadowspend.dto.AuthRequest;
import com.shadowspend.dto.AuthResponse;
import com.shadowspend.model.User;
import com.shadowspend.service.GmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GmailService gmailService;

    @PostMapping("/google")
    public AuthResponse googleAuth(@Valid @RequestBody AuthRequest request) {
        User user = gmailService.exchangeCodeForTokens(request.getCode());
        return new AuthResponse(user.getId(), user.getEmail(), user.getName(), user.getPictureUrl());
    }
}
