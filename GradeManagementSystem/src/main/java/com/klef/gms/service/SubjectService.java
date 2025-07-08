package com.klef.gms.service;

import com.klef.gms.model.Subject;
import com.klef.gms.model.Department;
import com.klef.gms.repo.SubjectRepo;
import com.klef.gms.repo.DepartmentRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepo subjectRepo;
    private final DepartmentRepo departmentRepo;

    public List<Subject> findAll() {
        return subjectRepo.findAll();
    }

    public Subject save(Subject subject, Long departmentId) {
        Department dept = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        subject.setDepartment(dept);

        if (subjectRepo.existsByName(subject.getName())) {
            throw new RuntimeException("Subject already exists");
        }

        return subjectRepo.save(subject);
    }
}
