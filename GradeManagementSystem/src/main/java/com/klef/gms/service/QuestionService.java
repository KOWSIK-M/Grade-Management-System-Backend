package com.klef.gms.service;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.klef.gms.model.*;
import com.klef.gms.repo.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepo questionRepo;
    private final UserRepo userRepo;
    private final SubjectRepo subjectRepo;
    private final DepartmentRepo departmentRepo;

    public Question addQuestion(Question question, Long userId, Long subjectId, Long departmentId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Subject subject = subjectRepo.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        Department department = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        question.setCreatedBy(user);
        question.setSubject(subject);
        question.setDepartment(department);

        // Link each option back to the question
        if (question.getOptions() != null) {
            question.getOptions().forEach(opt -> opt.setQuestion(question));
        }

        return questionRepo.save(question);
    }

    public List<Question> getAllQuestions() {
        return questionRepo.findAll();
    }

    public List<Question> getLatestQuestions() {
        return questionRepo.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Question> getQuestionById(Long id) {
        return questionRepo.findById(id);
    }

    public void deleteQuestion(Long id) {
        questionRepo.deleteById(id);
    }
    
    public Subject getSubjectById(Long id) {
        return subjectRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
    }

    public Department getDepartmentById(Long id) {
        return departmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
    }

    public Question saveQuestion(Question question) {
        return questionRepo.save(question);
    }

}
