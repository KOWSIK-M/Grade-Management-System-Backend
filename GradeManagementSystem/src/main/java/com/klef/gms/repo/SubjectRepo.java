package com.klef.gms.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.klef.gms.model.Subject;

import java.util.Optional;
import java.util.List;

@Repository
public interface SubjectRepo extends JpaRepository<Subject, Long> {
    boolean existsByName(String name);

    // Logger for default methods
    static final Logger logger = LoggerFactory.getLogger(SubjectRepo.class);

    default boolean safeExistsByName(String name) {
        logger.info("Checking if subject exists by name: {}", name);
        try {
            boolean exists = existsByName(name);
            if (exists) {
                logger.info("Subject '{}' exists.", name);
            } else {
                logger.info("Subject '{}' does not exist.", name);
            }
            return exists;
        } catch (Exception ex) {
            logger.error("Error while checking existence of subject '{}': {}", name, ex.getMessage(), ex);
            return false;
        }
    }

    default Optional<Subject> safeFindById(Long id) {
        logger.info("Attempting to find subject by id: {}", id);
        try {
            Optional<Subject> subject = findById(id);
            if (subject.isPresent()) {
                logger.info("Subject found for id: {}", id);
            } else {
                logger.warn("No subject found for id: {}", id);
            }
            return subject;
        } catch (Exception ex) {
            logger.error("Error while finding subject by id {}: {}", id, ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    default List<Subject> safeFindAll() {
        logger.info("Fetching all subjects safely.");
        try {
            List<Subject> subjects = findAll();
            logger.info("Fetched {} subjects.", subjects.size());
            return subjects;
        } catch (Exception ex) {
            logger.error("Error while fetching all subjects: {}", ex.getMessage(), ex);
            return List.of();
        }
    }
}