package com.klef.gms.repo;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import com.klef.gms.model.EmailVerificationToken;
import com.klef.gms.model.User;

public interface EmailVerificationTokenRepo extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUser(User user);

    // Default methods for logging and exception handling
    static final Logger logger = LoggerFactory.getLogger(EmailVerificationTokenRepo.class);

    default Optional<EmailVerificationToken> safeFindByToken(String token) {
        logger.info("Attempting to find EmailVerificationToken by token: {}", token);
        try {
            Optional<EmailVerificationToken> result = findByToken(token);
            if (result.isPresent()) {
                logger.info("Token found for: {}", token);
            } else {
                logger.warn("No token found for: {}", token);
            }
            return result;
        } catch (Exception ex) {
            logger.error("Error while finding token {}: {}", token, ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    default void safeDeleteByUser(User user) {
        logger.info("Attempting to delete EmailVerificationToken for user: {}", user.getEmail());
        try {
            deleteByUser(user);
            logger.info("Token deleted for user: {}", user.getEmail());
        } catch (Exception ex) {
            logger.error("Error while deleting token for user {}: {}", user.getEmail(), ex.getMessage(), ex);
        }
    }
}
