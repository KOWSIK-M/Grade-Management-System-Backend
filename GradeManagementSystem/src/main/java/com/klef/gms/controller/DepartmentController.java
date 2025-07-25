package com.klef.gms.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.klef.gms.model.Department;
import com.klef.gms.repo.DepartmentRepo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentRepo departmentRepo;

    @GetMapping
    public ResponseEntity<?> getAllDepartments() {
        logger.info("Received request to fetch all departments.");
        try {
            List<Department> departments = departmentRepo.findAll();
            logger.info("Successfully fetched {} departments.", departments.size());
            return ResponseEntity.ok(departments);
        } catch (Exception ex) {
            logger.error("Error occurred while fetching departments: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Unable to fetch departments at this time. Please try again later.");
        }
    }
}
