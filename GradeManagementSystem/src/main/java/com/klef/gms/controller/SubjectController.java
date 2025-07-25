package com.klef.gms.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.klef.gms.model.Department;
import com.klef.gms.model.Subject;
import com.klef.gms.model.SubjectType;
import com.klef.gms.repo.DepartmentRepo;
import com.klef.gms.repo.SubjectRepo;
import com.klef.gms.service.SubjectService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SubjectController {

    private static final Logger logger = LoggerFactory.getLogger(SubjectController.class);

    private final SubjectService subjectService;
    private final DepartmentRepo departmentRepo;
    private final SubjectRepo subjectRepo;

    @GetMapping
    public ResponseEntity<?> getAllSubjects() {
        logger.info("Received request to fetch all subjects.");
        try {
            List<Subject> subjects = subjectService.findAll();
            logger.info("Fetched {} subjects.", subjects.size());
            return ResponseEntity.ok(subjects);
        } catch (Exception ex) {
            logger.error("Error while fetching subjects: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Unable to fetch subjects at this time.");
        }
    }

    @PostMapping
    public ResponseEntity<?> createSubject(@RequestBody Map<String, String> request) {
        logger.info("Received request to create a new subject.");
        try {
            String name = request.get("name");
            String typeStr = request.get("type");
            String deptIdStr = request.get("departmentId");

            if (deptIdStr == null || deptIdStr.trim().isEmpty()) {
                logger.warn("Department ID is missing in the request.");
                return ResponseEntity.badRequest().body("Department ID is required");
            }

            Long deptId = Long.parseLong(deptIdStr);

            Department dept = departmentRepo.findById(deptId)
                .orElseThrow(() -> {
                    logger.warn("Department not found for ID: {}", deptId);
                    return new RuntimeException("Department not found");
                });

            SubjectType type;
            try {
                type = SubjectType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid subject type provided: {}", typeStr);
                return ResponseEntity.badRequest()
                        .body("Invalid subject type. Allowed: " + java.util.Arrays.toString(SubjectType.values()));
            }

            Subject subject = Subject.builder()
                .name(name)
                .type(type)
                .department(dept)
                .build();

            Subject saved = subjectRepo.save(subject);
            logger.info("Subject created successfully with ID: {}", saved.getId());
            return ResponseEntity.status(201).body(saved);
        } catch (Exception ex) {
            logger.error("Error while creating subject: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Failed to create subject. Please try again.");
        }
    }

    @GetMapping("/subject-types")
    public ResponseEntity<?> getSubjectTypes() {
        logger.info("Received request to fetch subject types.");
        try {
            SubjectType[] types = SubjectType.values();
            logger.info("Fetched {} subject types.", types.length);
            return ResponseEntity.ok(types);
        } catch (Exception ex) {
            logger.error("Error while fetching subject types: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Unable to fetch subject types at this time.");
        }
    }

    @GetMapping("/subjects-count")
    public ResponseEntity<?> getSubjectCount() {
        logger.info("Received request to fetch subject count.");
        try {
            long count = subjectService.subjectCount();
            logger.info("Total subjects count: {}", count);
            return ResponseEntity.ok(Collections.singletonMap("subjectCount", count));
        } catch (Exception ex) {
            logger.error("Error while fetching subject count: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Unable to fetch subject count at this time.");
        }
    }
}
