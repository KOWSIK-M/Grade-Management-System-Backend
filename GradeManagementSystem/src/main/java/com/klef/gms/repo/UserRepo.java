package com.klef.gms.repo;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.klef.gms.model.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    User findUserByEmail(String email);

    // Logger for default methods
    static final Logger logger = LoggerFactory.getLogger(UserRepo.class);

    default Optional<User> safeFindByEmail(String email) {
        logger.info("Attempting to find user by email: {}", email);
        try {
            Optional<User> user = findByEmail(email);
            if (user.isPresent()) {
                logger.info("User found for email: {}", email);
            } else {
                logger.warn("No user found for email: {}", email);
            }
            return user;
        } catch (Exception ex) {
            logger.error("Error while finding user by email {}: {}", email, ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    default User safeFindUserByEmail(String email) {
        logger.info("Attempting to find user (non-optional) by email: {}", email);
        try {
            User user = findUserByEmail(email);
            if (user != null) {
                logger.info("User found for email: {}", email);
            } else {
                logger.warn("No user found for email: {}", email);
            }
            return user;
        } catch (Exception ex) {
            logger.error("Error while finding user by email {}: {}", email, ex.getMessage(), ex);
            return null;
        }
    }
}
