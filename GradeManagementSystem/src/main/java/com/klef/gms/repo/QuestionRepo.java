package com.klef.gms.repo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.klef.gms.model.Question;
import com.klef.gms.model.User;

@Repository
public interface QuestionRepo extends JpaRepository<Question, Long> {
    List<Question> findAllByOrderByCreatedAtDesc(); // For dashboard (latest first)
    List<Question> findByCreatedBy(User user);

    // Logger for default methods
    static final Logger logger = LoggerFactory.getLogger(QuestionRepo.class);

    default List<Question> safeFindAllByOrderByCreatedAtDesc() {
        logger.info("Attempting to fetch all questions ordered by createdAt descending.");
        try {
            List<Question> questions = findAllByOrderByCreatedAtDesc();
            logger.info("Fetched {} questions.", questions.size());
            return questions;
        } catch (Exception ex) {
            logger.error("Error while fetching questions: {}", ex.getMessage(), ex);
            return List.of();
        }
    }

    default List<Question> safeFindByCreatedBy(User user) {
        logger.info("Attempting to fetch questions created by user: {}", user.getEmail());
        try {
            List<Question> questions = findByCreatedBy(user);
            logger.info("Fetched {} questions for user: {}", questions.size(), user.getEmail());
            return questions;
        } catch (Exception ex) {
            logger.error("Error while fetching questions for user {}: {}", user.getEmail(), ex.getMessage(), ex);
            return List.of();
        }
    }
}
