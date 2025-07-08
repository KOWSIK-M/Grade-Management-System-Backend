package com.klef.gms.controller;

import java.util.List;

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

    private final DepartmentRepo departmentRepo;

    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentRepo.findAll();
    }
}
