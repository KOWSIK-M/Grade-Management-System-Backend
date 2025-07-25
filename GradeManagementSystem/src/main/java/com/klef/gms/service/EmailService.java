package com.klef.gms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void send(String to, String subject, String body) {
        logger.info("Preparing to send email to: {} with subject: {}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception ex) {
            logger.error("Failed to send email to {}: {}", to, ex.getMessage(), ex);
        }
    }
}
