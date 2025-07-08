package com.klef.gms.controller;

import java.util.List;

import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    private final QuestionService questionService;
    private final UserRepo userRepo;

    @PostMapping("/add")
    public ResponseEntity<?> addQuestion(Principal principal, @RequestBody QuestionDTO payload) {

    	String email = principal.getName();
        User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

        // Correct variable name: payload
        Subject subject = questionService.getSubjectById(payload.getSubjectId());
        Department department = questionService.getDepartmentById(payload.getDepartmentId());

        Question question = payload.toQuestion(subject, department, user);
        Question saved = questionService.saveQuestion(question);

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Question>> getLatestQuestions() {
        return ResponseEntity.ok(questionService.getLatestQuestions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> getById(@PathVariable Long id) {
        return questionService.getQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/question-types")
    public QuestionType[] getQuestionTypes() {
        return QuestionType.values();
    }

}
