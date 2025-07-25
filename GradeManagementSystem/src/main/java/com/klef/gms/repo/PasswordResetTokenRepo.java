package com.klef.gms.repo;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import com.klef.gms.model.PasswordResetToken;

public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByEmailAndOtp(String email, String otp);

    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.email = :email")
    void deleteByEmail(@Param("email") String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryTime < :now")
    int deleteAllExpiredTokens(@Param("now") LocalDateTime now);

    // Logger for default methods
    static final Logger logger = LoggerFactory.getLogger(PasswordResetTokenRepo.class);

    default Optional<PasswordResetToken> safeFindByEmailAndOtp(String email, String otp) {
        logger.info("Attempting to find PasswordResetToken by email: {} and otp: {}", email, otp);
        try {
            Optional<PasswordResetToken> result = findByEmailAndOtp(email, otp);
            if (result.isPresent()) {
                logger.info("PasswordResetToken found for email: {}", email);
            } else {
                logger.warn("No PasswordResetToken found for email: {}", email);
            }
            return result;
        } catch (Exception ex) {
            logger.error("Error while finding PasswordResetToken for email {}: {}", email, ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    default void safeDeleteByEmail(String email) {
        logger.info("Attempting to delete PasswordResetToken for email: {}", email);
        try {
            deleteByEmail(email);
            logger.info("PasswordResetToken deleted for email: {}", email);
        } catch (Exception ex) {
            logger.error("Error while deleting PasswordResetToken for email {}: {}", email, ex.getMessage(), ex);
        }
    }

    default int safeDeleteAllExpiredTokens(LocalDateTime now) {
        logger.info("Attempting to delete all expired PasswordResetTokens before: {}", now);
        try {
            int deleted = deleteAllExpiredTokens(now);
            logger.info("Deleted {} expired PasswordResetTokens.", deleted);
            return deleted;
        } catch (Exception ex) {
            logger.error("Error while deleting expired PasswordResetTokens: {}", ex.getMessage(), ex);
            return 0;
        }
    }
}
