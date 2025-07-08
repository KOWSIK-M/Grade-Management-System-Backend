package com.klef.gms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	 private final SubjectService subjectService;
	 private final DepartmentRepo departmentRepo;
	 private final SubjectRepo subjectRepo;

	    @GetMapping
	    public List<Subject> getAllSubjects() {
	        return subjectService.findAll();
	    }

	    @PostMapping
	    public Subject createSubject(@RequestBody Map<String, String> request) {
	        String name = request.get("name");
	        String typeStr = request.get("type");
	        String deptIdStr = request.get("departmentId");

	        if (deptIdStr == null || deptIdStr.trim().isEmpty()) {
	            throw new IllegalArgumentException("Department ID is required");
	        }

	        Long deptId = Long.parseLong(deptIdStr);

	        Department dept = departmentRepo.findById(deptId)
	            .orElseThrow(() -> new RuntimeException("Department not found"));

	        SubjectType type;
	        try {
	            type = SubjectType.valueOf(typeStr.toUpperCase()); // Convert string to ENUM safely
	        } catch (IllegalArgumentException e) {
	            throw new RuntimeException("Invalid subject type. Allowed: " + java.util.Arrays.toString(SubjectType.values()));
	        }

	        Subject subject = Subject.builder()
	            .name(name)
	            .type(type)
	            .department(dept)
	            .build();

	        return subjectRepo.save(subject);
	    }



}
