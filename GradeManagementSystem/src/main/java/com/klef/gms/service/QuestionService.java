package com.klef.gms.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.klef.gms.model.*;
import com.klef.gms.repo.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepo questionRepo;
    private final UserRepo userRepo;
    private final SubjectRepo subjectRepo;
    private final DepartmentRepo departmentRepo;

    public Question addQuestion(Question question, Long userId, Long subjectId, Long departmentId) {
        logger.info("Adding a new question for userId: {}, subjectId: {}, departmentId: {}", userId, subjectId, departmentId);
        try {
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User not found for id: {}", userId);
                        return new RuntimeException("User not found");
                    });
            Subject subject = subjectRepo.findById(subjectId)
                    .orElseThrow(() -> {
                        logger.warn("Subject not found for id: {}", subjectId);
                        return new RuntimeException("Subject not found");
                    });
            Department department = departmentRepo.findById(departmentId)
                    .orElseThrow(() -> {
                        logger.warn("Department not found for id: {}", departmentId);
                        return new RuntimeException("Department not found");
                    });

            question.setCreatedBy(user);
            question.setSubject(subject);
            question.setDepartment(department);

            // Link each option back to the question
            if (question.getOptions() != null) {
                question.getOptions().forEach(opt -> opt.setQuestion(question));
            }

            Question saved = questionRepo.save(question);
            logger.info("Question saved successfully with id: {}", saved.getId());
            return saved;
        } catch (Exception ex) {
            logger.error("Error while adding question: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to add question", ex);
        }
    }

    public List<Question> getAllQuestions() {
        logger.info("Fetching all questions.");
        try {
            List<Question> questions = questionRepo.findAll();
            logger.info("Fetched {} questions.", questions.size());
            return questions;
        } catch (Exception ex) {
            logger.error("Error while fetching all questions: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch questions", ex);
        }
    }

    public List<Question> getLatestQuestions() {
        logger.info("Fetching latest questions.");
        try {
            List<Question> latest = questionRepo.findAllByOrderByCreatedAtDesc();
            logger.info("Fetched {} latest questions.", latest.size());
            return latest;
        } catch (Exception ex) {
            logger.error("Error while fetching latest questions: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch latest questions", ex);
        }
    }

    public Optional<Question> getQuestionById(Long id) {
        logger.info("Fetching question by id: {}", id);
        try {
            Optional<Question> question = questionRepo.findById(id);
            if (question.isPresent()) {
                logger.info("Question found for id: {}", id);
            } else {
                logger.warn("No question found for id: {}", id);
            }
            return question;
        } catch (Exception ex) {
            logger.error("Error while fetching question by id {}: {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch question by id", ex);
        }
    }

    public void deleteQuestion(Long id) {
        logger.info("Deleting question with id: {}", id);
        try {
            questionRepo.deleteById(id);
            logger.info("Question deleted with id: {}", id);
        } catch (Exception ex) {
            logger.error("Error while deleting question with id {}: {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete question", ex);
        }
    }
    
    public Subject getSubjectById(Long id) {
        logger.info("Fetching subject by id: {}", id);
        try {
            return subjectRepo.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Subject not found for id: {}", id);
                        return new RuntimeException("Subject not found");
                    });
        } catch (Exception ex) {
            logger.error("Error while fetching subject by id {}: {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch subject by id", ex);
        }
    }

    public Department getDepartmentById(Long id) {
        logger.info("Fetching department by id: {}", id);
        try {
            return departmentRepo.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Department not found for id: {}", id);
                        return new RuntimeException("Department not found");
                    });
        } catch (Exception ex) {
            logger.error("Error while fetching department by id {}: {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch department by id", ex);
        }
    }

    public Question saveQuestion(Question question) {
        logger.info("Saving question.");
        try {
            Question saved = questionRepo.save(question);
            logger.info("Question saved with id: {}", saved.getId());
            return saved;
        } catch (Exception ex) {
            logger.error("Error while saving question: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to save question", ex);
        }
    }

    public long questionCount() {
        logger.info("Counting total questions.");
        try {
            long count = questionRepo.count();
            logger.info("Total questions count: {}", count);
            return count;
        } catch (Exception ex) {
            logger.error("Error while counting questions: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to count questions", ex);
        }
    }
    
    public List<Question> getQuestionsByUser(User user) {
        logger.info("Fetching questions created by user: {}", user.getEmail());
        try {
            List<Question> questions = questionRepo.findByCreatedBy(user);
            logger.info("Fetched {} questions for user: {}", questions.size(), user.getEmail());
            return questions;
        } catch (Exception ex) {
            logger.error("Error while fetching questions for user {}: {}", user.getEmail(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to fetch user's questions", ex);
        }
    }

    public boolean deleteQuestionIfOwnedByUser(Long questionId, User user) {
        logger.info("Attempting to delete question with id: {} for user: {}", questionId, user.getEmail());
        try {
            Optional<Question> optionalQuestion = questionRepo.findById(questionId);
            if (optionalQuestion.isPresent()) {
                Question question = optionalQuestion.get();
                if (question.getCreatedBy().getId().equals(user.getId())) {
                    questionRepo.deleteById(questionId);
                    logger.info("Question with id: {} deleted by user: {}", questionId, user.getEmail());
                    return true;
                } else {
                    logger.warn("User: {} attempted to delete question id: {} but does not own it.", user.getEmail(), questionId);
                }
            } else {
                logger.warn("No question found with id: {} to delete.", questionId);
            }
            return false;
        } catch (Exception ex) {
            logger.error("Error while deleting question with id {} for user {}: {}", questionId, user.getEmail(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to delete question", ex);
        }
    }
}
