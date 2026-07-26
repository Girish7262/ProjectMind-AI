package com.acciobuild.organization.service.impl;

import com.acciobuild.organization.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service implementation managing asynchronous email notifications delivery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@acciobuild.com}")
    private String fromEmailAddress;

    @Async
    @Override
    public void sendInvitationEmail(
            String recipientEmail, 
            String organizationName, 
            String inviterName, 
            String invitationLink, 
            LocalDateTime expiresAt) {
        
        log.info("Asynchronously sending organization invitation email to: {}", recipientEmail);

        try {
            Context context = new Context();
            context.setVariable("organizationName", organizationName);
            context.setVariable("inviterName", inviterName);
            context.setVariable("invitationLink", invitationLink);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            context.setVariable("expirationTime", expiresAt.format(formatter));

            String htmlContent = templateEngine.process("invitation-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmailAddress);
            helper.setTo(recipientEmail);
            helper.setSubject("You are invited to join the organization " + organizationName + " on AccioBuild");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Invitation email successfully delivered to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to asynchronously deliver invitation email to: {}", recipientEmail, e);
        }
    }
}
