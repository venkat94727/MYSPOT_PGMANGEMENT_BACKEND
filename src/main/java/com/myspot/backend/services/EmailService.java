package com.myspot.backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.email.from-name:MySpot PG Team}")
    private String fromName;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.email.support-email:pg-support@myspot.com}")
    private String supportEmail;
    
    @Value("${app.email.website-url:https://myspot.com}")
    private String websiteUrl;
    
    @Async
    public void sendOtpEmail(String toEmail, String otp, String userName) {
        try {
            log.info("Sending OTP email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("MySpot PG - Email Verification OTP");
            
            String htmlContent = createOtpEmailTemplate(otp, userName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken, String userName) {
        try {
            log.info("Sending password reset email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("MySpot PG - Password Reset Request");
            
            String resetLink = websiteUrl + "/reset-password?token=" + resetToken;
            String htmlContent = createPasswordResetEmailTemplate(resetLink, userName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    private String createOtpEmailTemplate(String otp, String userName) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 10px;'>" +
                "<h2 style='color: #2c3e50;'>MySpot PG - Email Verification</h2>" +
                "<p>Hello " + userName + ",</p>" +
                "<p>Thank you for registering with MySpot PG Management. Please use the following OTP to verify your email address:</p>" +
                "<div style='background-color: #3498db; color: white; padding: 15px; border-radius: 5px; text-align: center; font-size: 24px; font-weight: bold; margin: 20px 0;'>" +
                otp +
                "</div>" +
                "<p>This OTP is valid for 5 minutes. If you didn't request this, please ignore this email.</p>" +
                "<p>Best regards,<br>MySpot PG Team</p>" +
                "<hr style='border: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='font-size: 12px; color: #666;'>" +
                "If you have any questions, please contact us at " + supportEmail +
                "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private String createPasswordResetEmailTemplate(String resetLink, String userName) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 10px;'>" +
                "<h2 style='color: #2c3e50;'>MySpot PG - Password Reset</h2>" +
                "<p>Hello " + userName + ",</p>" +
                "<p>You have requested to reset your password. Click the button below to reset your password:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + resetLink + "' style='background-color: #e74c3c; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>" +
                "Reset Password" +
                "</a>" +
                "</div>" +
                "<p>If the button doesn't work, copy and paste this link in your browser:</p>" +
                "<p style='word-break: break-all; color: #3498db;'>" + resetLink + "</p>" +
                "<p>This link will expire in 1 hour. If you didn't request this, please ignore this email.</p>" +
                "<p>Best regards,<br>MySpot PG Team</p>" +
                "<hr style='border: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='font-size: 12px; color: #666;'>" +
                "If you have any questions, please contact us at " + supportEmail +
                "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}