package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.service.EmailService;
import com.acciobuild.common.dto.ApiResponse;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service implementation sending welcome, verification, and reset password emails using JavaMailSender and Thymeleaf HTML templates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private ApiResponse<Void> sendHtmlEmail(String to, String subject, String templateName, Context context) {
        try {
            String htmlContent = templateEngine.process(templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Successfully sent HTML email '{}' to {}", templateName, to);
            
            return ApiResponse.<Void>builder()
                    .status(200)
                    .message("Email sent successfully.")
                    .build();
        } catch (Exception e) {
            log.error("Failed to send HTML email '{}' to {}: {}", templateName, to, e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .status(500)
                    .message("Failed to send email: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ApiResponse<Void> sendVerificationEmail(User user, String token) {
        log.info("Sending verification email to: {}", user.getEmail());
        Context context = new Context();
        context.setVariable("name", user.getFirstName() + " " + user.getLastName());
        context.setVariable("verificationUrl", "http://localhost:8080/api/v1/auth/verify-email?token=" + token);
        
        return sendHtmlEmail(user.getEmail(), "AccioBuild - Verify Email Account", "verification-email", context);
    }

    @Override
    public ApiResponse<Void> sendResetPasswordEmail(User user, String token) {
        log.info("Sending password reset email to: {}", user.getEmail());
        Context context = new Context();
        context.setVariable("name", user.getFirstName() + " " + user.getLastName());
        context.setVariable("resetCode", token);
        // Expiration in minutes (e.g. 60 minutes)
        context.setVariable("expiryTime", "60 minutes");
        
        return sendHtmlEmail(user.getEmail(), "AccioBuild - Reset Password Request", "forgot-password", context);
    }

    @Override
    public ApiResponse<Void> sendWelcomeEmail(User user) {
        log.info("Sending welcome email to: {}", user.getEmail());
        Context context = new Context();
        context.setVariable("name", user.getFirstName() + " " + user.getLastName());
        
        return sendHtmlEmail(user.getEmail(), "AccioBuild - Welcome to Platform", "welcome-email", context);
    }

    @Override
    public ApiResponse<Void> sendPasswordChangedEmail(User user) {
        log.info("Sending password changed confirmation email to: {}", user.getEmail());
        Context context = new Context();
        context.setVariable("name", user.getFirstName() + " " + user.getLastName());
        context.setVariable("changeTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return sendHtmlEmail(user.getEmail(), "AccioBuild - Password Changed Successfully", "password-changed", context);
    }

    @Override
    public ApiResponse<Void> sendPasswordResetFailedEmail(User user, String reason, String ipAddress, String device) {
        log.info("Sending password reset failed alert email to: {}", user.getEmail());
        Context context = new Context();
        context.setVariable("name", user.getFirstName() + " " + user.getLastName());
        context.setVariable("reason", reason);
        context.setVariable("ipAddress", ipAddress);
        context.setVariable("device", device);
        context.setVariable("attemptTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return sendHtmlEmail(user.getEmail(), "AccioBuild - Security Alert: Password Reset Attempt Failed", "password-reset-failed", context);
    }
}
