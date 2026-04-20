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

    /**
     * 发送密码重置邮件（生产版本）
     */
    public void sendResetPasswordEmail(String toEmail, String resetToken) {
        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);           // 发件人
        message.setTo(toEmail);               // 收件人
        message.setSubject("【Heritage Platform】密码重置请求");  // 邮件标题
        message.setText("您好，\n\n" +
                "您收到了这封邮件是因为您（或其他人）请求重置 Heritage Platform 的密码。\n\n" +
                "请点击下面的链接重置密码（链接 30 分钟内有效）：\n" +
                resetLink + "\n\n" +
                "如果您没有请求重置密码，请忽略这封邮件。\n\n" +
                "感谢使用我们的平台！");

        mailSender.send(message);
        System.out.println("✅ 密码重置邮件已发送至：" + toEmail);
    }
}