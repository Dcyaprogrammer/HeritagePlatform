package com.heritage.platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


    public void sendResetPasswordEmail(String toEmail, String resetToken) {
        String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[Heritage Platform] Password Reset Request");
        message.setText("Hello,\n\n" +
                "We received a request to reset your Heritage Platform password.\n\n" +
                "Use the link below to reset your password. This link will expire in 30 minutes:\n" +
                resetLink + "\n\n" +
                "If you did not request a password reset, you can ignore this email.\n\n" +
                "Thank you for using Heritage Platform.");

        mailSender.send(message);
        System.out.println("Password reset email delivered to: " + toEmail);
    }
}
