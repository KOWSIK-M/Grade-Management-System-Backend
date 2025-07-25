package com.klef.gms.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.klef.gms.repo.PasswordResetTokenRepo;

@Service
public class TokenCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupService.class);

    @Autowired
    private PasswordResetTokenRepo tokenRepo;

    @Scheduled(fixedRate = 10 * 60 * 1000) // every 10 minutes
    public void cleanExpiredTokens() {
        logger.info("Starting cleanup of expired password reset tokens.");
        try {
            int deletedCount = tokenRepo.deleteAllExpiredTokens(LocalDateTime.now());
            logger.info("Expired tokens cleaned at: {}. Total deleted: {}", LocalDateTime.now(), deletedCount);
        } catch (Exception ex) {
            logger.error("Error occurred during expired token cleanup: {}", ex.getMessage(), ex);
        }
    }
}   