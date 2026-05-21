package com.shadowspend.service;

import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to ShadowSpend 🚀");

        message.setText(
                "Hi there,\n\n" +
                "Welcome to ShadowSpend 👋\n\n" +
                "You're all set to uncover hidden subscriptions and take control of your spending.\n\n" +
                "🔍 What you can do next:\n" +
                "• Scan your Gmail to detect recurring charges\n" +
                "• Track monthly spending automatically\n" +
                "• Get alerts before renewals hit your account\n\n" +
                "Start by heading to your dashboard and clicking 'Scan Emails'.\n\n" +
                "If you didn't sign up, you can safely ignore this email.\n\n" +
                "— Team ShadowSpend"
        );

        mailSender.send(message);
    }
}
