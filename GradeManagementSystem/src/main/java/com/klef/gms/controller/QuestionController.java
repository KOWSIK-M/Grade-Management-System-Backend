package com.klef.gms.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.klef.gms.model.Department;
import com.klef.gms.model.Question;
import com.klef.gms.model.QuestionDTO;
import com.klef.gms.model.QuestionType;
import com.klef.gms.model.Subject;
import com.klef.gms.model.User;
import com.klef.gms.repo.UserRepo;
import com.klef.gms.service.QuestionService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    private final QuestionService questionService;
    private final UserRepo userRepo;

    @PostMapping("/add")
    public ResponseEntity<?> addQuestion(Principal principal, @RequestBody QuestionDTO payload) {
        logger.info("Received request to add a new question.");
        try {
            String email = principal.getName();
            logger.debug("Fetching user by email: {}", email);
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Subject subject = questionService.getSubjectById(payload.getSubjectId());
            Department department = questionService.getDepartmentById(payload.getDepartmentId());

            Question question = payload.toQuestion(subject, department, user);
            Question saved = questionService.saveQuestion(question);

            logger.info("Question added successfully with ID: {}", saved.getId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception ex) {
            logger.error("Error while adding question: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add question. Please try again.");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllQuestions() {
        logger.info("Received request to fetch all questions.");
        try {
            List<Question> questions = questionService.getAllQuestions();
            logger.info("Fetched {} questions.", questions.size());
            return ResponseEntity.ok(questions);
        } catch (Exception ex) {
            logger.error("Error while fetching questions: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch questions at this time.");
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestQuestions() {
        logger.info("Received request to fetch latest questions.");
        try {
            List<Question> latestQuestions = questionService.getLatestQuestions();
            logger.info("Fetched {} latest questions.", latestQuestions.size());
            return ResponseEntity.ok(latestQuestions);
        } catch (Exception ex) {
            logger.error("Error while fetching latest questions: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch latest questions at this time.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        logger.info("Received request to fetch question by ID: {}", id);
        try {
            return questionService.getQuestionById(id)
                    .map(question -> {
                        logger.info("Question found with ID: {}", id);
                        return ResponseEntity.ok(question);
                    })
                    .orElseGet(() -> {
                        logger.warn("Question not found with ID: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception ex) {
            logger.error("Error while fetching question by ID: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch question at this time.");
        }
    }

    @GetMapping("/question-types")
    public ResponseEntity<?> getQuestionTypes() {
        logger.info("Received request to fetch question types.");
        try {
            QuestionType[] types = QuestionType.values();
            logger.info("Fetched {} question types.", types.length);
            return ResponseEntity.ok(types);
        } catch (Exception ex) {
            logger.error("Error while fetching question types: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch question types at this time.");
        }
    }

    @GetMapping("/questions-count")
    public ResponseEntity<?> getQuestionCount() {
        logger.info("Received request to fetch question count.");
        try {
            long count = questionService.questionCount();
            logger.info("Total questions count: {}", count);
            return ResponseEntity.ok(Collections.singletonMap("questionCount", count));
        } catch (Exception ex) {
            logger.error("Error while fetching question count: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch question count at this time.");
        }
    }

    @GetMapping("/my-questions")
    public ResponseEntity<?> getMyQuestions(Principal principal) {
        logger.info("Received request to fetch questions for current user.");
        try {
            String email = principal.getName();
            logger.debug("Fetching user by email: {}", email);
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Question> myQuestions = questionService.getQuestionsByUser(user);
            logger.info("Fetched {} questions for user: {}", myQuestions.size(), email);
            return ResponseEntity.ok(myQuestions);
        } catch (Exception ex) {
            logger.error("Error while fetching user's questions: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to fetch your questions at this time.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id, Principal principal) {
        logger.info("Received request to delete question with ID: {}", id);
        try {
            String email = principal.getName();
            logger.debug("Fetching user by email: {}", email);
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean deleted = questionService.deleteQuestionIfOwnedByUser(id, user);
            if (deleted) {
                logger.info("Question with ID: {} deleted by user: {}", id, email);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("User: {} attempted to delete question ID: {} but was forbidden.", email, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not allowed to delete this question.");
            }
        } catch (Exception ex) {
            logger.error("Error while deleting question: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to delete question at this time.");
        }
    }


}
